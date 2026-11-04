package com.dipsource.repository.feed;

import com.dipsource.entity.feed.Feed;
import com.dipsource.global.common.LikePattern;
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

    /** 1단계: LOWER() + LIKE 부분 일치 */
    default Page<Feed> searchByKeyword(String keyword, Pageable pageable) {
        return searchByLikePattern(LikePattern.contains(keyword), pageable);
    }

    /** 공개(ACTIVE)이면서 삭제되지 않은 피드 중 본문에 패턴이 들어 있는 피드. 최신순. */
    @Query(value = """
            SELECT f FROM Feed f
            WHERE f.status = com.dipsource.entity.feed.FeedStatus.ACTIVE
              AND f.deletedAt IS NULL
              AND LOWER(f.content) LIKE LOWER(:pattern) ESCAPE '\\'
            ORDER BY f.createdAt DESC, f.id DESC
            """,
            countQuery = """
            SELECT COUNT(f) FROM Feed f
            WHERE f.status = com.dipsource.entity.feed.FeedStatus.ACTIVE
              AND f.deletedAt IS NULL
              AND LOWER(f.content) LIKE LOWER(:pattern) ESCAPE '\\'
            """)
    Page<Feed> searchByLikePattern(@Param("pattern") String pattern, Pageable pageable);
}
