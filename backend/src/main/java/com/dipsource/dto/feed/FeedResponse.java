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
        Object demo,
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
    public static FeedResponse from(Feed feed, User author) {
        return from(feed, author, false, false, false);
    }

    /** 로그인용 — 좋아요/북마크/팔로우 상태 반영 */
    public static FeedResponse from(Feed feed, User author, boolean liked, boolean bookmarked, boolean following) {
        return new FeedResponse(
                feed.getId(),
                UserSummaryResponse.from(author),
                feed.getContent(),
                null,
                feed.getImageUrls(),
                feed.getLinkUrl(),
                feed.getLikeCount(),
                feed.getCommentCount(),
                feed.getRepostCount(),
                liked,
                false,
                bookmarked,
                following,
                feed.getPollOptions(),
                feed.getCreatedAt()
        );
    }
}
