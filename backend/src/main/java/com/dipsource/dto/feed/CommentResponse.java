package com.dipsource.dto.feed;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.feed.FeedComment;
import com.dipsource.entity.user.User;
import java.time.LocalDateTime;
import java.util.List;

/** 댓글 응답 */
public record CommentResponse(
        Long id,
        UserSummaryResponse author,
        String content,
        Long parentId,
        int likeCount,
        boolean liked,
        int replyCount,
        boolean edited,
        LocalDateTime createdAt,
        List<CommentResponse> replies
) {
    /** 최상위 댓글 — 대댓글 목록 포함 */
    public static CommentResponse from(FeedComment comment, User author, boolean liked, List<CommentResponse> replies) {
        return new CommentResponse(
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
    public static CommentResponse from(FeedComment comment, User author, boolean liked) {
        return new CommentResponse(
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
