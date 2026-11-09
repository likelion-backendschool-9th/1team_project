package com.dipsource.dto.demo;

import java.util.List;

/** 데모 피드백 커서 페이지네이션 래퍼 */
public record DemoCommentCursorResponse(
        List<DemoCommentResponse> content,
        String nextCursor,
        boolean hasNext,
        int size
) {
    public static DemoCommentCursorResponse of(List<DemoCommentResponse> content, boolean hasNext) {
        String nextCursor = hasNext && !content.isEmpty() ? content.get(content.size() - 1).id().toString() : null;
        return new DemoCommentCursorResponse(content, nextCursor, hasNext, content.size());
    }
}
