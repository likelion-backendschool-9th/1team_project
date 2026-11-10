package com.dipsource.dto.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoStatus;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedComment;
import com.dipsource.entity.feed.FeedStatus;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostComment;
import com.dipsource.entity.post.PostStatus;

public record AdminContentResponse(
        Long id,
        String title,
        String author,
        String createdAt,
        String metric,
        int reported,
        String status) {

    public static AdminContentResponse ofFeed(Feed feed, String author, int reported) {
        return new AdminContentResponse(
                feed.getId(),
                clip(feed.getContent()),
                author,
                format(feed.getCreatedAt()),
                "좋아요 " + feed.getLikeCount(),
                reported,
                feed.getStatus() == FeedStatus.HIDDEN ? "hidden" : "visible");
    }

    public static AdminContentResponse ofPost(Post post, String author, int reported) {
        return new AdminContentResponse(
                post.getId(),
                clip(post.getTitle()),
                author,
                format(post.getCreatedAt()),
                "조회 " + post.getViewCount(),
                reported,
                post.getStatus() == PostStatus.HIDDEN ? "hidden" : "visible");
    }

    public static AdminContentResponse ofComment(FeedComment comment, String author, int reported) {
        return new AdminContentResponse(
                comment.getId(),
                clip(comment.getContent()),
                author,
                format(comment.getCreatedAt()),
                "피드 #" + comment.getFeed().getId(),
                reported,
                "visible");
    }

    public static AdminContentResponse ofPostComment(PostComment comment, String author, int reported) {
        return new AdminContentResponse(
                comment.getId(),
                clip(comment.getContent()),
                author,
                format(comment.getCreatedAt()),
                "글 #" + comment.getPost().getId(),
                reported,
                "visible");
    }

    public static AdminContentResponse ofDemo(Demo demo, String author, int reported) {
        return new AdminContentResponse(
                demo.getId(),
                clip(demo.getTitle()),
                author,
                format(demo.getCreatedAt()),
                "체험 " + demo.getPlayCount(),
                reported,
                demo.getStatus() == DemoStatus.HIDDEN ? "hidden" : "visible");
    }

    private static String clip(String text) {
        String value = text == null ? "" : text;
        return value.length() <= 50 ? value : value.substring(0, 50) + "...";
    }

    private static String format(LocalDateTime value) {
        return value == null ? null : value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
