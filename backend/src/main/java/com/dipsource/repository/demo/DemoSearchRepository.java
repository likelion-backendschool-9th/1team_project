package com.dipsource.repository.demo;

import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.entity.demo.Demo;
import com.dipsource.global.common.LikePattern;
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
}
