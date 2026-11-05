package com.dipsource.dto.feed;

import java.util.List;

/** 댓글 커서 페이지네이션 래퍼 */
public record CommentCursorResponse(
        List<CommentResponse> content,
        String nextCursor,
        boolean hasNext,
        int size
) {
    public static CommentCursorResponse of(List<CommentResponse> content, int requestedSize) {
        boolean hasNext = content.size() >= requestedSize;
        String nextCursor = null;

        if (hasNext && !content.isEmpty()) {
            CommentResponse last = content.get(content.size() - 1);
            nextCursor = last.id().toString();
        }

        return new CommentCursorResponse(content, nextCursor, hasNext, content.size());
    }
}
