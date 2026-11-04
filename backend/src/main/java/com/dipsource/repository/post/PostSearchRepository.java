package com.dipsource.repository.post;

import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.entity.post.Post;
import com.dipsource.global.common.LikePattern;
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
}
