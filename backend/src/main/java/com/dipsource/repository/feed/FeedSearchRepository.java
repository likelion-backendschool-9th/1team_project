package com.dipsource.repository.feed;

import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchSignal;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchType;
import com.dipsource.entity.feed.Feed;
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
 * 피드 검색 전용 조회. 검색 방식을 바꿀 때는 이 파일만 바꾼다.
 * 피드 CRUD·추천 리포지토리와 섞이지 않도록 따로 둔다.
 */
public interface FeedSearchRepository extends Repository<Feed, Long> {

    /** 2단계: 단어별 LIKE + 관련도 점수 (PostgreSQL native SQL) */
    default Page<Feed> search(SearchCondition condition, Pageable pageable) {
        return searchByLikePatterns(
                condition.keywords().stream().map(LikePattern::contains).toArray(String[]::new),
                condition.phrase().toLowerCase(Locale.ROOT),
                LikePattern.contains(condition.phrase().toLowerCase(Locale.ROOT)),
                condition.operator() == SearchOperator.AND,
                condition.sort() == SearchSort.RELEVANCE,
                condition.exclusions().hiddenIds(SearchType.FEED),
                condition.exclusions().blockedIds(),
                pageable);
    }

    /**
     * 공개(ACTIVE)이면서 삭제되지 않은 피드 중 본문에 단어가 들어 있는 피드.
     * 피드는 제목이 없어서 본문을 블로그 제목처럼 점수 낸다.
     * 관련도 점수 = 본문이 검색어와 같음 100 + 본문에 검색어 전체 30 + 본문 단어당 10.
     * 파라미터와 m(LATERAL)의 뜻은 {@code PostSearchRepository}와 같다.
     */
    @Query(nativeQuery = true, value = """
            SELECT f.* FROM feeds f
            CROSS JOIN LATERAL (
                SELECT count(*) FILTER (WHERE lower(f.content) LIKE t.k) AS matched
                FROM unnest(CAST(:patterns AS text[])) AS t(k)
            ) m
            WHERE f.status = 'ACTIVE'
              AND f.deleted_at IS NULL
              AND f.id <> ALL(CAST(:excludedIds AS bigint[]))
              AND f.user_id <> ALL(CAST(:excludedAuthors AS bigint[]))
              AND m.matched >= CASE WHEN :matchAll THEN cardinality(CAST(:patterns AS text[])) ELSE 1 END
            ORDER BY CASE WHEN :relevance THEN
                         CASE WHEN lower(f.content) = :phrase THEN 100 ELSE 0 END
                       + CASE WHEN lower(f.content) LIKE :phrasePattern THEN 30 ELSE 0 END
                       + m.matched * 10
                     END DESC,
                     f.created_at DESC, f.id DESC
            """,
            countQuery = """
            SELECT count(*) FROM feeds f
            CROSS JOIN LATERAL (
                SELECT count(*) FILTER (WHERE lower(f.content) LIKE t.k) AS matched
                FROM unnest(CAST(:patterns AS text[])) AS t(k)
            ) m
            WHERE f.status = 'ACTIVE'
              AND f.deleted_at IS NULL
              AND f.id <> ALL(CAST(:excludedIds AS bigint[]))
              AND f.user_id <> ALL(CAST(:excludedAuthors AS bigint[]))
              AND m.matched >= CASE WHEN :matchAll THEN cardinality(CAST(:patterns AS text[])) ELSE 1 END
            """)
    Page<Feed> searchByLikePatterns(
            @Param("patterns") String[] patterns,
            @Param("phrase") String phrase,
            @Param("phrasePattern") String phrasePattern,
            @Param("matchAll") boolean matchAll,
            @Param("relevance") boolean relevance,
            @Param("excludedIds") Long[] excludedIds,
            @Param("excludedAuthors") Long[] excludedAuthors,
            Pageable pageable);

    /** 재정렬 신호. 공개 상태가 아닌 피드는 돌려주지 않아서, 인덱스가 늦어도 여기서 걸러진다. 피드에는 조회수가 없다 */
    @Query(nativeQuery = true, value = """
            SELECT f.id AS id, f.user_id AS authorId, f.like_count AS likeCount, 0 AS viewCount,
                   CAST(extract(epoch FROM (now() - f.created_at)) AS double precision) / 86400 AS ageDays,
                   (SELECT count(*) FROM follows x WHERE x.following_id = f.user_id) AS followerCount
            FROM feeds f
            WHERE f.id IN (:ids) AND f.status = 'ACTIVE' AND f.deleted_at IS NULL
            """)
    List<SearchSignal> findSignals(@Param("ids") Collection<Long> ids);

    List<Feed> findByIdIn(Collection<Long> ids);
}
