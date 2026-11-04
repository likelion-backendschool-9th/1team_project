package com.dipsource.repository.post;

import com.dipsource.entity.post.Post;
import com.dipsource.global.common.LikePattern;
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

    /** 1단계: LOWER() + LIKE 부분 일치 */
    default Page<Post> searchByKeyword(String keyword, Pageable pageable) {
        return searchByLikePattern(LikePattern.contains(keyword), pageable);
    }

    /**
     * 공개(ACTIVE)이면서 삭제되지 않은 글 중 제목 또는 본문에 패턴이 들어 있는 글. 최신순.
     * 유료 글은 본문이 검색으로 새지 않도록 제목만 비교한다.
     */
    @Query(value = """
            SELECT p FROM Post p
            WHERE p.status = com.dipsource.entity.post.PostStatus.ACTIVE
              AND p.deletedAt IS NULL
              AND (LOWER(p.title) LIKE LOWER(:pattern) ESCAPE '\\'
                   OR (p.paid = false AND LOWER(p.content) LIKE LOWER(:pattern) ESCAPE '\\'))
            ORDER BY p.createdAt DESC, p.id DESC
            """,
            countQuery = """
            SELECT COUNT(p) FROM Post p
            WHERE p.status = com.dipsource.entity.post.PostStatus.ACTIVE
              AND p.deletedAt IS NULL
              AND (LOWER(p.title) LIKE LOWER(:pattern) ESCAPE '\\'
                   OR (p.paid = false AND LOWER(p.content) LIKE LOWER(:pattern) ESCAPE '\\'))
            """)
    Page<Post> searchByLikePattern(@Param("pattern") String pattern, Pageable pageable);
}
