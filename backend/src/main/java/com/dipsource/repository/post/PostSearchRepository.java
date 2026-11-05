package com.dipsource.repository.post;

import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchTermCandidate;
import com.dipsource.entity.post.Post;
import com.dipsource.global.common.LikePattern;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * 블로그 글 검색 전용 조회. 검색 방식을 바꿀 때는 이 파일만 바꾼다.
 * 글 CRUD용 리포지토리와 섞이지 않도록 따로 둔다.
 */
public interface PostSearchRepository extends Repository<Post, Long> {

    /** 2단계: 단어별 LIKE + 관련도 점수 (PostgreSQL native SQL) */
    default Page<Post> search(SearchCondition condition, Pageable pageable) {
        return searchByLikePatterns(
                condition.keywords().stream().map(LikePattern::contains).toArray(String[]::new),
                condition.phrase().toLowerCase(Locale.ROOT),
                LikePattern.contains(condition.phrase().toLowerCase(Locale.ROOT)),
                condition.operator() == SearchOperator.AND,
                condition.sort() == SearchSort.RELEVANCE,
                pageable);
    }

    /**
     * 공개(ACTIVE)이면서 삭제되지 않은 글 중 제목 또는 본문에 단어가 들어 있는 글.
     * 유료 글은 본문이 검색으로 새지 않도록 제목만 비교하고, 본문 점수도 주지 않는다.
     *
     * <p>m(LATERAL)은 글 한 건마다 단어 배열을 펼쳐서 단어가 몇 개 들어 있는지 센다.
     * <ul>
     *   <li>matched: 제목이나 본문에 들어 있는 단어 수. and면 단어 수와 같아야 하고, or면 1 이상</li>
     *   <li>title_hits / body_hits: 제목 / 본문에 들어 있는 단어 수</li>
     * </ul>
     * 관련도 점수 = 제목이 검색어와 같음 100 + 제목에 검색어 전체 30 + 제목 단어당 10 + 본문 단어당 3.
     * PostgreSQL LIKE의 기본 이스케이프 문자가 \ 라서 ESCAPE를 따로 쓰지 않는다.
     *
     * @param patterns      단어별 LIKE 패턴(소문자, 이스케이프 완료)
     * @param phrase        소문자 검색어 전체
     * @param phrasePattern 검색어 전체의 LIKE 패턴
     * @param matchAll      true면 and, false면 or
     * @param relevance     true면 관련도순, false면 최신순
     */
    @Query(nativeQuery = true, value = """
            SELECT p.* FROM posts p
            CROSS JOIN LATERAL (
                SELECT count(*) FILTER (WHERE lower(p.title) LIKE t.k
                                           OR (NOT p.is_paid AND lower(p.content) LIKE t.k)) AS matched,
                       count(*) FILTER (WHERE lower(p.title) LIKE t.k) AS title_hits,
                       count(*) FILTER (WHERE NOT p.is_paid AND lower(p.content) LIKE t.k) AS body_hits
                FROM unnest(CAST(:patterns AS text[])) AS t(k)
            ) m
            WHERE p.status = 'ACTIVE'
              AND p.deleted_at IS NULL
              AND m.matched >= CASE WHEN :matchAll THEN cardinality(CAST(:patterns AS text[])) ELSE 1 END
            ORDER BY CASE WHEN :relevance THEN
                         CASE WHEN lower(p.title) = :phrase THEN 100 ELSE 0 END
                       + CASE WHEN lower(p.title) LIKE :phrasePattern THEN 30 ELSE 0 END
                       + m.title_hits * 10
                       + m.body_hits * 3
                     END DESC,
                     p.created_at DESC, p.id DESC
            """,
            countQuery = """
            SELECT count(*) FROM posts p
            CROSS JOIN LATERAL (
                SELECT count(*) FILTER (WHERE lower(p.title) LIKE t.k
                                           OR (NOT p.is_paid AND lower(p.content) LIKE t.k)) AS matched
                FROM unnest(CAST(:patterns AS text[])) AS t(k)
            ) m
            WHERE p.status = 'ACTIVE'
              AND p.deleted_at IS NULL
              AND m.matched >= CASE WHEN :matchAll THEN cardinality(CAST(:patterns AS text[])) ELSE 1 END
            """)
    Page<Post> searchByLikePatterns(
            @Param("patterns") String[] patterns,
            @Param("phrase") String phrase,
            @Param("phrasePattern") String phrasePattern,
            @Param("matchAll") boolean matchAll,
            @Param("relevance") boolean relevance,
            Pageable pageable);

    /**
     * 3단계: 오타 교정 후보. 공개(ACTIVE)이면서 삭제되지 않은 글의 제목에서 검색 단어와 가까운 단어 두 개(가까운 순).
     * 유료 글도 제목은 공개라서 사전에 넣는다. 본문은 유료 글 보호를 위해 쓰지 않는다.
     * 검색 단어를 포함하는 단어가 있으면 그것이 먼저 나온다(오타가 아니라는 뜻).
     * 두 개를 주는 이유: 검색 단어 자체가 사전에 있을 때(sprng로 쓴 글) 그다음 후보(spring)까지 봐야 제안할 수 있다.
     *
     * @param word        소문자 검색 단어
     * @param maxDistance 허용하는 최대 편집 거리
     */
    default List<SearchTermCandidate> findClosestTerms(String word, int maxDistance) {
        return findClosestTitleTerms(word, LikePattern.contains(word), maxDistance);
    }

    /**
     * 단어 사전(w)은 제목을 영문·숫자·한글·일부 기호(_ . # + -)가 아닌 글자로 잘라 만들고, 단어별 등장 횟수를 센다.
     * 거리가 같으면 등장 횟수가 많은 단어를 고른다. 제목에 오타(spirng)가 섞여 있어도 바른 철자(spring)가 더 자주 나온다.
     * levenshtein_less_equal은 거리가 maxDistance를 넘으면 계산을 멈춰서 levenshtein보다 빠르다.
     * 글자 수 차이가 maxDistance보다 크면 거리도 그보다 크므로 먼저 거른다.
     */
    @Query(nativeQuery = true, value = """
            SELECT w.term AS term,
                   w.term LIKE :pattern AS contained,
                   levenshtein(:word, w.term) AS distance,
                   w.frequency AS frequency,
                   CAST(similarity(:word, w.term) AS double precision) AS similarity
            FROM (
                SELECT s.term, count(*) AS frequency
                FROM posts p
                CROSS JOIN LATERAL regexp_split_to_table(lower(p.title), '[^a-z0-9가-힣_.#+-]+') AS s(term)
                WHERE p.status = 'ACTIVE'
                  AND p.deleted_at IS NULL
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
}
