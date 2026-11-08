package com.dipsource.dto.post;

import java.util.List;

/** 블로그 댓글 커서 페이지네이션 래퍼 */
public record PostCommentCursorResponse(
        List<PostCommentResponse> content,
        String nextCursor,
        boolean hasNext,
        int size
) {
    public static PostCommentCursorResponse of(List<PostCommentResponse> content, int requestedSize) {
        boolean hasNext = content.size() >= requestedSize;
        String nextCursor = null;

        if (hasNext && !content.isEmpty()) {
            PostCommentResponse last = content.get(content.size() - 1);
            nextCursor = last.id().toString();
        }

        return new PostCommentCursorResponse(content, nextCursor, hasNext, content.size());
    }
}
