package com.dipsource.repository.demo;

import com.dipsource.dto.search.AutocompletePrefix;
import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchSignal;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchType;
import com.dipsource.dto.search.SearchTermCandidate;
import com.dipsource.dto.search.SearchTermFrequency;
import com.dipsource.entity.demo.Demo;
import com.dipsource.global.common.LikePattern;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
                condition.exclusions().hiddenIds(SearchType.DEMO),
                condition.exclusions().blockedIds(),
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
              AND d.id <> ALL(CAST(:excludedIds AS bigint[]))
              AND d.user_id <> ALL(CAST(:excludedAuthors AS bigint[]))
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
              AND d.id <> ALL(CAST(:excludedIds AS bigint[]))
              AND d.user_id <> ALL(CAST(:excludedAuthors AS bigint[]))
              AND m.matched >= CASE WHEN :matchAll THEN cardinality(CAST(:patterns AS text[])) ELSE 1 END
            """)
    Page<Demo> searchByLikePatterns(
            @Param("patterns") String[] patterns,
            @Param("phrase") String phrase,
            @Param("phrasePattern") String phrasePattern,
            @Param("matchAll") boolean matchAll,
            @Param("relevance") boolean relevance,
            @Param("excludedIds") Long[] excludedIds,
            @Param("excludedAuthors") Long[] excludedAuthors,
            Pageable pageable);

    /**
     * 3단계: 오타 교정 후보. 공개(ACTIVE)이면서 삭제되지 않은 데모의 제목에서 검색 단어와 가까운 단어 두 개(가까운 순).
     * 검색 단어를 포함하는 단어가 있으면 그것이 먼저 나온다(오타가 아니라는 뜻).
     * 두 개를 주는 이유: 검색 단어 자체가 사전에 있을 때(sprng로 쓴 글) 그다음 후보(spring)까지 봐야 제안할 수 있다.
     *
     * @param word        소문자 검색 단어
     * @param maxDistance 허용하는 최대 편집 거리
     */
    default List<SearchTermCandidate> findClosestTerms(String word, int maxDistance) {
        return findClosestTitleTerms(word, LikePattern.contains(word), maxDistance);
    }

    /** 쿼리 설명은 {@code PostSearchRepository#findClosestTitleTerms}과 같다. */
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
            LIMIT 2
            """)
    List<SearchTermCandidate> findClosestTitleTerms(
            @Param("word") String word,
            @Param("pattern") String pattern,
            @Param("maxDistance") int maxDistance);

    /**
     * 4단계: 자동완성 단어 후보. 공개(ACTIVE)이면서 삭제되지 않은 데모의 제목에서 접두어로 시작하는 단어와 등장 횟수.
     * 쿼리 설명은 {@code PostSearchRepository#findTermsByPrefix}와 같다.
     *
     * @param prefix 마지막 단어의 접두어 조건(한글 조합 중 받침 처리 포함)
     * @param limit  최대 개수
     */
    default List<SearchTermFrequency> findTermsByPrefix(AutocompletePrefix prefix, int limit) {
        return findTitleTermsByPrefix(prefix.pattern(), prefix.composing(), prefix.stemPattern(), prefix.stemLength(),
                prefix.nextFrom(), prefix.nextTo(), limit);
    }

    @Query(nativeQuery = true, value = """
            SELECT s.term AS term, count(*) AS frequency
            FROM demos d
            CROSS JOIN LATERAL regexp_split_to_table(lower(d.title), '[^a-z0-9가-힣_.#+-]+') AS s(term)
            WHERE length(s.term) BETWEEN 1 AND 50
                  AND d.status = 'ACTIVE'
                  AND d.deleted_at IS NULL
              AND (s.term LIKE :pattern
                   OR (:composing
                       AND s.term LIKE :stemPattern
                       AND substr(s.term, :stemLength + 1, 1) COLLATE "C" BETWEEN :nextFrom AND :nextTo))
            GROUP BY s.term
            ORDER BY frequency DESC, length(s.term), s.term
            LIMIT :limit
            """)
    List<SearchTermFrequency> findTitleTermsByPrefix(
            @Param("pattern") String pattern,
            @Param("composing") boolean composing,
            @Param("stemPattern") String stemPattern,
            @Param("stemLength") int stemLength,
            @Param("nextFrom") String nextFrom,
            @Param("nextTo") String nextTo,
            @Param("limit") int limit);

    /** 재정렬 신호. 공개 상태가 아닌 데모는 돌려주지 않아서, 인덱스가 늦어도 여기서 걸러진다 */
    @Query(nativeQuery = true, value = """
            SELECT d.id AS id, d.user_id AS authorId, d.like_count AS likeCount, d.play_count AS viewCount,
                   CAST(extract(epoch FROM (now() - d.created_at)) AS double precision) / 86400 AS ageDays,
                   (SELECT count(*) FROM follows f WHERE f.following_id = d.user_id) AS followerCount
            FROM demos d
            WHERE d.id IN (:ids) AND d.status = 'ACTIVE' AND d.deleted_at IS NULL
            """)
    List<SearchSignal> findSignals(@Param("ids") Collection<Long> ids);

    List<Demo> findByIdIn(Collection<Long> ids);
}
