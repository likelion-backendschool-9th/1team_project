package com.dipsource.dto.post;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.PostComment;
import com.dipsource.entity.user.User;
import java.time.LocalDateTime;
import java.util.List;

/** 블로그 댓글 응답 */
public record PostCommentResponse(
        Long id,
        UserSummaryResponse author,
        String content,
        Long parentId,
        int likeCount,
        boolean liked,
        int replyCount,
        boolean edited,
        LocalDateTime createdAt,
        List<PostCommentResponse> replies
) {
    /** 최상위 댓글 — 대댓글 목록 포함 */
    public static PostCommentResponse from(PostComment comment, User author, boolean liked, List<PostCommentResponse> replies) {
        return new PostCommentResponse(
                comment.getId(),
                UserSummaryResponse.from(author),
                comment.getDeletedAt() != null ? null : comment.getContent(),
                comment.getParent() == null ? null : comment.getParent().getId(),
                comment.getLikeCount(),
                liked,
                replies.size(),
                comment.getUpdatedAt() != null && comment.getDeletedAt() == null,
                comment.getCreatedAt(),
                replies
        );
    }

    /** 대댓글용 (replies 없음) */
    public static PostCommentResponse from(PostComment comment, User author, boolean liked) {
        return new PostCommentResponse(
                comment.getId(),
                UserSummaryResponse.from(author),
                comment.getContent(),
                comment.getParent() == null ? null : comment.getParent().getId(),
                comment.getLikeCount(),
                liked,
                0,
                comment.getUpdatedAt() != null,
                comment.getCreatedAt(),
                null
        );
    }
}
