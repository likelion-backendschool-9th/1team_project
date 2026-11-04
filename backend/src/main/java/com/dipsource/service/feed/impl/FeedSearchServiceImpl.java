package com.dipsource.service.feed.impl;

import com.dipsource.dto.feed.FeedSearchItemResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.feed.Feed;
import com.dipsource.global.common.PageResponse;
import com.dipsource.repository.feed.FeedSearchRepository;
import com.dipsource.service.feed.FeedSearchService;
import com.dipsource.service.user.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link FeedSearchService} 구현체. */
@Service
@RequiredArgsConstructor
public class FeedSearchServiceImpl implements FeedSearchService {

    private final FeedSearchRepository feedSearchRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FeedSearchItemResponse> search(String keyword, Pageable pageable) {
        Page<Feed> feeds = feedSearchRepository.searchByKeyword(keyword, pageable);
        Map<Long, UserSummaryResponse> authors = userService.getSummaries(
                feeds.getContent().stream().map(Feed::getUserId).distinct().toList());
        return PageResponse.of(feeds, feed -> {
            UserSummaryResponse author = authors.get(feed.getUserId());
            return new FeedSearchItemResponse(
                    feed.getId(),
                    feed.getContent(),
                    feed.getUserId(),
                    author == null ? null : author.nickname(),
                    feed.getLikeCount(),
                    feed.getCommentCount(),
                    feed.getCreatedAt()
            );
        });
    }
}
