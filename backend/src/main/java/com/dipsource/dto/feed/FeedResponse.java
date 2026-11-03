package com.dipsource.dto.feed;

import com.dipsource.entity.feed.Feed;
import java.time.LocalDateTime;
import java.util.List;

public record FeedResponse(
        Long id,
        Long userId,
        String content,
        int likeCount,
        int commentCount,
        int repostCount,
        List<String> pollOptions,
        LocalDateTime createdAt
) {
    public static FeedResponse from(Feed feed) {
        return new FeedResponse(
                feed.getId(),
                feed.getUserId(),
                feed.getContent(),
                feed.getLikeCount(),
                feed.getCommentCount(),
                feed.getRepostCount(),
                feed.getPollOptions(),
                feed.getCreatedAt()
        );
    }
}
