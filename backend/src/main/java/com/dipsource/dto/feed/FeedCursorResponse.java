package com.dipsource.dto.feed;

import java.util.List;

public record FeedCursorResponse(
        List<FeedResponse> content,
        String nextCursor,
        boolean hasNext,
        int size
) {
    public static FeedCursorResponse of(List<FeedResponse> content, int requestedSize) {
        boolean hasNext = content.size() >= requestedSize;
        String nextCursor = null;

        if (hasNext && !content.isEmpty()) {
            FeedResponse last = content.get(content.size() - 1);
            nextCursor = last.id().toString();
        }

        return new FeedCursorResponse(content, nextCursor, hasNext, content.size());
    }
}
