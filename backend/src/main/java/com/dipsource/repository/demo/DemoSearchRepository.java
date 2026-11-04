package com.dipsource.repository.demo;

import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchTermCandidate;
import com.dipsource.entity.demo.Demo;
import com.dipsource.global.common.LikePattern;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * 데모 검색 전용 조회. 검색 방식을 바꿀 때는 이 파일만 바꾼다.
 * 데모 CRUD용 리포지토리와 섞이지 않도록 따로 둔다.
 */
public interface DemoSearchRepository extends Repository<Demo, Long> {

    /** 2단계: 단어별 LIKE + 관련도 점수 (PostgreSQL native SQL) */
    default Page<Demo> search(SearchCondition condition, Pageable pageable) {
        return searchByLikePatterns(
                condition.keywords().stream().map(LikePattern::contains).toArray(String[]::new),
                condition.phrase().toLowerCase(Locale.ROOT),
                LikePattern.contains(condition.phrase().toLowerCase(Locale.ROOT)),
                condition.operator() == SearchOperator.AND,
                condition.sort() == SearchSort.RELEVANCE,
                pageable);
    }

    /**
     * 공개(ACTIVE)이면서 삭제되지 않은 데모 중 제목 또는 설명에 단어가 들어 있는 데모.
     * 관련도 점수 = 제목이 검색어와 같음 100 + 제목에 검색어 전체 30 + 제목 단어당 10 + 설명 단어당 3.
     * 파라미터와 m(LATERAL)의 뜻은 {@code PostSearchRepository}와 같다.
     */
    @Query(nativeQuery = true, value = """
            SELECT d.* FROM demos d
            CROSS JOIN LATERAL (
                SELECT count(*) FILTER (WHERE lower(d.title) LIKE t.k OR lower(d.description) LIKE t.k) AS matched,
                       count(*) FILTER (WHERE lower(d.title) LIKE t.k) AS title_hits,
                       count(*) FILTER (WHERE lower(d.description) LIKE t.k) AS body_hits
                FROM unnest(CAST(:patterns AS text[])) AS t(k)
            ) m
            WHERE d.status = 'ACTIVE'
              AND d.deleted_at IS NULL
              AND m.matched >= CASE WHEN :matchAll THEN cardinality(CAST(:patterns AS text[])) ELSE 1 END
            ORDER BY CASE WHEN :relevance THEN
                         CASE WHEN lower(d.title) = :phrase THEN 100 ELSE 0 END
                       + CASE WHEN lower(d.title) LIKE :phrasePattern THEN 30 ELSE 0 END
                       + m.title_hits * 10
                       + m.body_hits * 3
                     END DESC,
                     d.created_at DESC, d.id DESC
            """,
            countQuery = """
            SELECT count(*) FROM demos d
            CROSS JOIN LATERAL (
                SELECT count(*) FILTER (WHERE lower(d.title) LIKE t.k OR lower(d.description) LIKE t.k) AS matched
                FROM unnest(CAST(:patterns AS text[])) AS t(k)
            ) m
            WHERE d.status = 'ACTIVE'
              AND d.deleted_at IS NULL
              AND m.matched >= CASE WHEN :matchAll THEN cardinality(CAST(:patterns AS text[])) ELSE 1 END
            """)
    Page<Demo> searchByLikePatterns(
            @Param("patterns") String[] patterns,
            @Param("phrase") String phrase,
            @Param("phrasePattern") String phrasePattern,
            @Param("matchAll") boolean matchAll,
            @Param("relevance") boolean relevance,
            Pageable pageable);

    /**
     * 3단계: 오타 교정 후보. 공개(ACTIVE)이면서 삭제되지 않은 데모의 제목에서 검색 단어와 가장 가까운 단어 하나.
     * 검색 단어를 포함하는 단어가 있으면 그것이 먼저 나온다(오타가 아니라는 뜻).
     *
     * @param word        소문자 검색 단어
     * @param maxDistance 허용하는 최대 편집 거리
     */
    default Optional<SearchTermCandidate> findClosestTerm(String word, int maxDistance) {
        return findClosestTitleTerm(word, LikePattern.contains(word), maxDistance);
    }

    /** 쿼리 설명은 {@code PostSearchRepository#findClosestTitleTerm}과 같다. */
    @Query(nativeQuery = true, value = """
            SELECT w.term AS term,
                   w.term LIKE :pattern AS contained,
                   levenshtein(:word, w.term) AS distance,
                   w.frequency AS frequency,
                   CAST(similarity(:word, w.term) AS double precision) AS similarity
            FROM (
                SELECT s.term, count(*) AS frequency
                FROM demos d
                CROSS JOIN LATERAL regexp_split_to_table(lower(d.title), '[^a-z0-9가-힣_.#+-]+') AS s(term)
                WHERE d.status = 'ACTIVE'
                  AND d.deleted_at IS NULL
                GROUP BY s.term
            ) w
            WHERE length(w.term) BETWEEN 1 AND 50
              AND (w.term LIKE :pattern
                   OR (abs(length(w.term) - length(:word)) <= :maxDistance
                       AND levenshtein_less_equal(:word, w.term, :maxDistance) <= :maxDistance))
            ORDER BY contained DESC, distance, frequency DESC, similarity DESC, term
            LIMIT 1
            """)
    Optional<SearchTermCandidate> findClosestTitleTerm(
            @Param("word") String word,
            @Param("pattern") String pattern,
            @Param("maxDistance") int maxDistance);
}
