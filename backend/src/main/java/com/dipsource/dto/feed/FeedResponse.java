package com.dipsource.dto.feed;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.user.User;
import java.time.LocalDateTime;
import java.util.List;

public record FeedResponse(
        Long id,
        UserSummaryResponse author,
        String content,
        FeedDemoResponse demo,
        List<String> imageUrls,
        String linkUrl,
        int likeCount,
        int commentCount,
        int repostCount,
        boolean liked,
        boolean reposted,
        boolean bookmarked,
        boolean following,
        List<String> pollOptions,
        LocalDateTime createdAt
) {
    /** 비로그인용 — 상호작용 상태 전부 false */
    public static FeedResponse from(Feed feed, User author, FeedDemoResponse demo) {
        return from(feed, author, demo, false, false, false, false);
    }

    /** 로그인용 — 좋아요/리포스트/북마크/팔로우 상태 반영 */
    public static FeedResponse from(Feed feed, User author, FeedDemoResponse demo,
                                    boolean liked, boolean reposted, boolean bookmarked, boolean following) {
        return new FeedResponse(
                feed.getId(),
                UserSummaryResponse.from(author),
                feed.getContent(),
                demo,
                feed.getImageUrls(),
                feed.getLinkUrl(),
                feed.getLikeCount(),
                feed.getCommentCount(),
                feed.getRepostCount(),
                liked,
                reposted,
                bookmarked,
                following,
                feed.getPollOptions(),
                feed.getCreatedAt()
        );
    }
}
