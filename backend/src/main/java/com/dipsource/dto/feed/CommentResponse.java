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
        int replyCount,
        boolean edited,
        LocalDateTime createdAt
) {
    public static CommentResponse from(FeedComment comment, User author, int replyCount) {
        return new CommentResponse(
                comment.getId(),
                UserSummaryResponse.from(author),
                comment.getContent(),
                comment.getParent() == null ? null : comment.getParent().getId(),
                comment.getLikeCount(),
                replyCount,
                comment.getUpdatedAt() != null,
                comment.getCreatedAt()
        );
    }

    /** 대댓글용 (replyCount 불필요) */
    public static CommentResponse from(FeedComment comment, User author) {
        return from(comment, author, 0);
    }
}
