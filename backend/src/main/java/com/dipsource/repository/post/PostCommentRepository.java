package com.dipsource.repository.post;

import com.dipsource.entity.post.PostComment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** post_comments 테이블 접근 */
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    /**
     * 글의 표시 가능한 최상위 댓글 목록 (최신순, 커서 페이지네이션)
     * 삭제됐고 활성 대댓글도 없는 댓글은 DB에서 제외 — hasNext 계산 정확도 보장
     * size+1개를 요청해 hasNext 여부를 판단하고, Service에서 첫 size개만 처리
     * 숨길 회원의 댓글도 DB에서 제외 — 조회 후에 거르면 페이지가 비어 커서가 끊긴다
     */
    @Query(value = """
            SELECT * FROM post_comments pc
            WHERE pc.post_id = :postId
              AND pc.parent_id IS NULL
              AND (:cursor IS NULL OR pc.id < :cursor)
              AND pc.user_id <> ALL(CAST(:hiddenUserIds AS bigint[]))
              AND (pc.deleted_at IS NULL
                   OR EXISTS (
                       SELECT 1 FROM post_comments r
                       WHERE r.parent_id = pc.id AND r.deleted_at IS NULL
                         AND r.user_id <> ALL(CAST(:hiddenUserIds AS bigint[]))
                   ))
            ORDER BY pc.id DESC
            LIMIT :size
            """, nativeQuery = true)
    List<PostComment> findTopLevelCommentsVisible(
            @Param("postId") Long postId,
            @Param("cursor") Long cursor,
            @Param("hiddenUserIds") Long[] hiddenUserIds,
            @Param("size") int size);

    /** 댓글 소프트 삭제 (deleted_at IS NULL 조건으로 원자적 처리) — 실제 변경된 행 수 반환 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PostComment c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.id = :commentId AND c.deletedAt IS NULL")
    int softDeleteIfActive(@Param("commentId") Long commentId);

    /** 여러 부모 댓글의 대댓글을 한 번에 조회 (N+1 방지) */
    @Query(value = """
            SELECT * FROM post_comments
            WHERE parent_id IN (:parentIds)
              AND deleted_at IS NULL
            ORDER BY id ASC
            """, nativeQuery = true)
    List<PostComment> findRepliesByParentIds(@Param("parentIds") List<Long> parentIds);

    /** 댓글 수 +1 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    int incrementCommentCount(@Param("postId") Long postId);

    /** 댓글 수 -1 (0 미만 방지) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId AND p.commentCount > 0")
    int decrementCommentCount(@Param("postId") Long postId);

    /** 댓글 좋아요 수 +1 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PostComment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    int incrementCommentLikeCount(@Param("commentId") Long commentId);

    /** 댓글 좋아요 수 -1 (0 미만 방지) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PostComment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId AND c.likeCount > 0")
    int decrementCommentLikeCount(@Param("commentId") Long commentId);

    /** 관리자 블로그 댓글 목록. 삭제된 댓글은 빼고, 본문·작성자 닉네임 검색. */
    @Query(
            value = """
                    SELECT c FROM PostComment c JOIN FETCH c.post JOIN User u ON u.id = c.userId
                    WHERE c.deletedAt IS NULL
                      AND (:q IS NULL OR LOWER(c.content) LIKE :q ESCAPE '\\'
                          OR LOWER(u.nickname) LIKE :q ESCAPE '\\')
                    ORDER BY c.createdAt DESC, c.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(c) FROM PostComment c JOIN User u ON u.id = c.userId
                    WHERE c.deletedAt IS NULL
                      AND (:q IS NULL OR LOWER(c.content) LIKE :q ESCAPE '\\'
                          OR LOWER(u.nickname) LIKE :q ESCAPE '\\')
                    """)
    Page<PostComment> searchForAdmin(@Param("q") String q, Pageable pageable);
}
