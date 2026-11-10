package com.dipsource.dto.post;

import java.util.List;

/** 블로그 댓글 커서 페이지네이션 래퍼 */
public record PostCommentCursorResponse(
        List<PostCommentResponse> content,
        String nextCursor,
        boolean hasNext,
        int size
) {
    /** hasNext는 Service에서 size+1 조회 결과로 판단해 명시적으로 전달 */
    public static PostCommentCursorResponse of(List<PostCommentResponse> content, boolean hasNext) {
        String nextCursor = null;

        if (hasNext && !content.isEmpty()) {
            PostCommentResponse last = content.get(content.size() - 1);
            nextCursor = last.id().toString();
        }

        return new PostCommentCursorResponse(content, nextCursor, hasNext, content.size());
    }
}
