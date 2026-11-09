package com.dipsource.dto.demo;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.demo.DemoFeedback;
import com.dipsource.entity.user.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/** 데모 피드백 응답 */
public record DemoCommentResponse(
        Long id,
        UserSummaryResponse author,
        String type,
        String content,
        Long parentId,
        int likeCount,
        boolean liked,
        boolean edited,
        LocalDateTime createdAt,
        List<DemoCommentResponse> replies
) {
    public static DemoCommentResponse of(DemoFeedback feedback, User author, boolean liked, List<DemoCommentResponse> replies) {
        return new DemoCommentResponse(
                feedback.getId(),
                UserSummaryResponse.from(author),
                feedback.getType().name().toLowerCase(Locale.ROOT),
                feedback.getDeletedAt() != null ? null : feedback.getContent(),
                feedback.getParent() == null ? null : feedback.getParent().getId(),
                feedback.getLikeCount(),
                liked,
                feedback.getUpdatedAt() != null && feedback.getDeletedAt() == null,
                feedback.getCreatedAt(),
                replies);
    }
}
