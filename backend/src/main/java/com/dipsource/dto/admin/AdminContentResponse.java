package com.dipsource.dto.admin;

import java.time.format.DateTimeFormatter;

import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedStatus;

public record AdminContentResponse(
        Long id,
        String title,
        String author,
        String createdAt,
        String metric,
        int reported,
        String status) {
    public static AdminContentResponse ofFeed(Feed feed, String author, int reported) {
        String content = feed.getContent() == null ? "" : feed.getContent();
        String title = content.length() <= 50 ? content : content.substring(0, 50) + "...";
        return new AdminContentResponse(
                feed.getId(),
                title,
                author,
                feed.getCreatedAt() == null
                        ? null
                        : feed.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "좋아요 " + feed.getLikeCount(),
                reported,
                feed.getStatus() == FeedStatus.HIDDEN ? "hidden" : "visible");
    }
}
