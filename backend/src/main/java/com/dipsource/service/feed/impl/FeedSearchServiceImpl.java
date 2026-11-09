package com.dipsource.service.feed.impl;

import com.dipsource.dto.feed.FeedSearchItemResponse;
import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchSignal;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.feed.Feed;
import com.dipsource.global.common.PageResponse;
import com.dipsource.repository.feed.FeedSearchRepository;
import com.dipsource.service.feed.FeedSearchService;
import com.dipsource.service.user.UserService;
import io.micrometer.core.annotation.Timed;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link FeedSearchService} 구현체. */
@Service
@Timed(value = "search.query", description = "검색 쿼리 메서드별 실행 시간 (통합 검색 5단계 측정)")
@RequiredArgsConstructor
public class FeedSearchServiceImpl implements FeedSearchService {

    private final FeedSearchRepository feedSearchRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FeedSearchItemResponse> search(SearchCondition condition, Pageable pageable) {
        Page<Feed> feeds = feedSearchRepository.search(condition, pageable);
        Map<Long, UserSummaryResponse> authors = userService.getSummaries(
                feeds.getContent().stream().map(Feed::getUserId).distinct().toList());
        return PageResponse.of(feeds, feed -> toSearchItem(feed, authors.get(feed.getUserId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchSignal> findSignals(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : feedSearchRepository.findSignals(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedSearchItemResponse> findItems(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Feed> feeds = feedSearchRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(Feed::getId, Function.identity()));
        Map<Long, UserSummaryResponse> authors = userService.getSummaries(
                feeds.values().stream().map(Feed::getUserId).distinct().toList());
        return ids.stream().map(feeds::get).filter(Objects::nonNull)
                .map(feed -> toSearchItem(feed, authors.get(feed.getUserId())))
                .toList();
    }

    private static FeedSearchItemResponse toSearchItem(Feed feed, UserSummaryResponse author) {
        return new FeedSearchItemResponse(
                feed.getId(),
                feed.getContent(),
                feed.getUserId(),
                author == null ? null : author.nickname(),
                feed.getLikeCount(),
                feed.getCommentCount(),
                feed.getCreatedAt()
        );
    }
}
