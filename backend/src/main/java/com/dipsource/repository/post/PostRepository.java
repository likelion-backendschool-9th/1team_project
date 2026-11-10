package com.dipsource.repository.post;

import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostCategory;
import com.dipsource.entity.post.PostStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** posts 테이블 접근 (글 CRUD). 통합 검색은 PostSearchRepository가 따로 맡는다. */
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 블로그 목록. 분류와 검색어로 거른다. 정렬은 pageable로 받는다.
     * pattern은 소문자 LIKE 패턴(검색하지 않으면 "%"). 유료 글은 본문이 새지 않게 제목만 비교한다.
     */
    @Query("""
            SELECT p FROM Post p
            WHERE p.status = :status
              AND p.category IN :categories
              AND (LOWER(p.title) LIKE :pattern ESCAPE '\\'
                   OR (p.paid = false AND LOWER(p.content) LIKE :pattern ESCAPE '\\'))
              AND (:viewerId IS NULL OR (
                    p.userId NOT IN (SELECT b.id.blockedId FROM Block b WHERE b.id.blockerId = :viewerId)
                AND p.userId NOT IN (SELECT b.id.blockerId FROM Block b WHERE b.id.blockedId = :viewerId)))
            """)
    Page<Post> findList(
            @Param("status") PostStatus status,
            @Param("categories") Collection<PostCategory> categories,
            @Param("pattern") String pattern,
            @Param("viewerId") Long viewerId,
            Pageable pageable);

    /** 한 회원의 블로그 글 목록 */
    Page<Post> findByUserIdAndStatus(Long userId, PostStatus status, Pageable pageable);

    long countByUserIdAndStatus(Long userId, PostStatus status);

    /** 한 회원의 글 조회수 합. 글이 없으면 0 */
    @Query("SELECT COALESCE(SUM(p.viewCount), 0) FROM Post p WHERE p.userId = :userId AND p.status = :status")
    long sumViewCountByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PostStatus status);

    @Query("""
            SELECT p.userId, COUNT(p) FROM Post p
            WHERE p.status = :status AND p.userId IN :userIds
            GROUP BY p.userId
            """)
    List<Object[]> countByUserIdsAndStatus(
            @Param("userIds") Collection<Long> userIds,
            @Param("status") PostStatus status);

    boolean existsByIdAndUserIdAndStatus(Long id, Long userId, PostStatus status);

    /** 조회수 +1. 동시에 여러 명이 봐도 빠지지 않게 UPDATE 한 번으로 올린다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    int incrementViewCount(@Param("postId") Long postId);

    /** 좋아요 수 +1 (race-safe atomic UPDATE) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    int incrementLikeCount(@Param("postId") Long postId);

    /** 좋아요 수 -1. 0 미만으로 내려가지 않음 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId AND p.likeCount > 0")
    int decrementLikeCount(@Param("postId") Long postId);

    /** 관리자 글 목록. 삭제(DELETED)만 빼고, 제목·본문·작성자 닉네임 검색. */
    @Query("""
            SELECT p FROM Post p JOIN User u ON u.id = p.userId
            WHERE p.status <> com.dipsource.entity.post.PostStatus.DELETED
              AND (:q IS NULL OR LOWER(p.title) LIKE :q ESCAPE '\\'
                  OR LOWER(p.content) LIKE :q ESCAPE '\\'
                  OR LOWER(u.nickname) LIKE :q ESCAPE '\\')
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    Page<Post> searchForAdmin(@Param("q") String q, Pageable pageable);
}
