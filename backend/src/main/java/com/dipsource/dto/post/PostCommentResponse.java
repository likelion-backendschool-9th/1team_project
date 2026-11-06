package com.dipsource.dto.post;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.PostComment;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 블로그 댓글 (프론트 PostComment). 최상위 댓글에만 replies가 있다.
 *
 * @param deleted 지운 댓글인데 답글이 남아 자리만 보여 주는 경우 true. 이때 내용은 안내 문구
 */
public record PostCommentResponse(
        Long id,
        UserSummaryResponse author,
        String content,
        LocalDateTime createdAt,
        int likeCount,
        boolean liked,
        boolean deleted,
        List<PostCommentResponse> replies
) {

    /** 지운 댓글 자리에 보여 줄 문구 */
    public static final String DELETED_CONTENT = "삭제된 댓글이에요.";

    public static PostCommentResponse of(
            PostComment comment, UserSummaryResponse author, boolean liked, List<PostCommentResponse> replies) {
        boolean deleted = comment.getDeletedAt() != null;
        return new PostCommentResponse(
                comment.getId(),
                author,
                deleted ? DELETED_CONTENT : comment.getContent(),
                comment.getCreatedAt(),
                deleted ? 0 : comment.getLikeCount(),
                !deleted && liked,
                deleted,
                replies
        );
    }
}
