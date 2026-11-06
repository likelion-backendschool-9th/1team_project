package com.dipsource.service.post;

import com.dipsource.dto.post.PostCommentResponse;
import com.dipsource.dto.post.PostIdResponse;
import com.dipsource.dto.post.WriteCommentRequest;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * 블로그 댓글·답글·댓글 좋아요.
 * 글이 삭제(또는 숨김)되면 조회를 포함해 모든 동작이 막힌다(404).
 */
public interface PostCommentService {

    /** 최상위 댓글(오래된 순)과 답글. viewerId는 liked 표시용, 비로그인이면 null */
    PageResponse<PostCommentResponse> getComments(Long postId, Long viewerId, Pageable pageable);

    /** 댓글·답글 작성. 답글은 같은 글의 최상위 댓글에만 (1단계) */
    PostIdResponse create(Long userId, Long postId, WriteCommentRequest request);

    /** 작성자만 */
    void update(Long userId, Long commentId, String content);

    /** 작성자 또는 글 주인. soft delete */
    void delete(Long userId, Long commentId);

    /** 멱등 */
    void like(Long userId, Long commentId);

    /** 멱등 */
    void unlike(Long userId, Long commentId);
}
