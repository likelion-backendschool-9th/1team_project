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
    /** 작성자 정보 포함 변환 */
    public static FeedResponse from(Feed feed, User author) {
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
                false,
                false,
                false,
                false,
                feed.getPollOptions(),
                feed.getCreatedAt()
        );
    }
}
