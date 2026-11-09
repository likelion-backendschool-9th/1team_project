package com.dipsource.service.search.impl;

import com.dipsource.dto.search.SearchHiddenItemResponse;
import com.dipsource.dto.search.SearchSignal;
import com.dipsource.dto.search.SearchType;
import com.dipsource.dto.demo.DemoSearchItemResponse;
import com.dipsource.dto.feed.FeedSearchItemResponse;
import com.dipsource.dto.post.PostSearchItemResponse;
import com.dipsource.dto.user.UserSearchItemResponse;
import com.dipsource.entity.search.SearchHiddenResult;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.search.SearchHiddenResultRepository;
import com.dipsource.repository.search.SearchVisitRepository;
import com.dipsource.service.demo.DemoSearchService;
import com.dipsource.service.feed.FeedSearchService;
import com.dipsource.service.post.PostSearchService;
import com.dipsource.service.search.SearchPreferenceService;
import com.dipsource.service.search.query.TextNormalizer;
import com.dipsource.service.user.UserSearchService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link SearchPreferenceService} 구현체. */
@Service
@RequiredArgsConstructor
public class SearchPreferenceServiceImpl implements SearchPreferenceService {

    private static final int FEED_LABEL_LENGTH = 40;

    private final SearchHiddenResultRepository hiddenRepository;
    private final SearchVisitRepository visitRepository;
    private final PostSearchService postSearchService;
    private final DemoSearchService demoSearchService;
    private final FeedSearchService feedSearchService;
    private final UserSearchService userSearchService;
    private final SearchProperties properties;
    private final ObjectProvider<EngineSearchService> engine;

    @Override
    @Transactional
    public void hide(Long userId, String targetType, Long targetId) {
        SearchType type = parse(targetType);
        requireVisible(type, targetId);
        hiddenRepository.insertIgnore(userId, type.name(), targetId);
        invalidate(userId);
    }

    @Override
    @Transactional
    public void unhide(Long userId, String targetType, Long targetId) {
        hiddenRepository.delete(userId, parse(targetType).name(), targetId);
        invalidate(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SearchHiddenItemResponse> hiddenList(Long userId, Pageable pageable) {
        Page<SearchHiddenResult> page = hiddenRepository.findByUserIdOrderByIdDesc(userId, pageable);
        Map<String, Map<Long, String>> labels = page.getContent().stream()
                .collect(Collectors.groupingBy(SearchHiddenResult::getTargetType,
                        Collectors.mapping(SearchHiddenResult::getTargetId, Collectors.toList())))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> labels(SearchType.valueOf(entry.getKey()), entry.getValue())));
        return PageResponse.of(page, hidden -> new SearchHiddenItemResponse(
                hidden.getTargetType().toLowerCase(java.util.Locale.ROOT), hidden.getTargetId(),
                labels.getOrDefault(hidden.getTargetType(), Map.of()).get(hidden.getTargetId()),
                hidden.getCreatedAt()));
    }

    @Override
    @Transactional
    public void recordVisit(Long userId, String targetType, Long targetId, String query) {
        String normalized = TextNormalizer.normalize(query);
        if (normalized.isEmpty()) {
            return;
        }
        SearchType type = parse(targetType);
        requireVisible(type, targetId);
        visitRepository.recordVisit(userId, type.name(), targetId, normalized,
                LocalDateTime.now().minus(properties.getPersonalization().getVisitDedupWindow()));
    }

    private void invalidate(Long userId) {
        EngineSearchService service = engine.getIfAvailable();
        if (service != null) {
            service.invalidateViewer(userId);
        }
    }

    private void requireVisible(SearchType type, Long targetId) {
        List<SearchSignal> found = switch (type) {
            case POST -> postSearchService.findSignals(List.of(targetId));
            case DEMO -> demoSearchService.findSignals(List.of(targetId));
            case FEED -> feedSearchService.findSignals(List.of(targetId));
            case USER -> userSearchService.findSignals(List.of(targetId));
            case ALL -> throw new IllegalArgumentException("ALL은 개별 대상이 아니다");
        };
        if (found.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "대상을 찾을 수 없어요");
        }
    }

    private Map<Long, String> labels(SearchType type, List<Long> ids) {
        return switch (type) {
            case POST -> toMap(postSearchService.findItems(ids), PostSearchItemResponse::id, PostSearchItemResponse::title);
            case DEMO -> toMap(demoSearchService.findItems(ids), DemoSearchItemResponse::id, DemoSearchItemResponse::title);
            case FEED -> toMap(feedSearchService.findItems(ids), FeedSearchItemResponse::id, feed -> abbreviate(feed.content()));
            case USER -> toMap(userSearchService.findItems(ids), UserSearchItemResponse::id, UserSearchItemResponse::nickname);
            case ALL -> Map.of();
        };
    }

    private static <T> Map<Long, String> toMap(List<T> items, Function<T, Long> id, Function<T, String> label) {
        return items.stream().filter(Objects::nonNull).collect(Collectors.toMap(id, label));
    }

    private static String abbreviate(String text) {
        return text.length() <= FEED_LABEL_LENGTH ? text : text.substring(0, FEED_LABEL_LENGTH) + "…";
    }

    private static SearchType parse(String targetType) {
        SearchType type = SearchType.from(targetType);
        if (type == SearchType.ALL) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "대상은 post, demo, feed, user 중 하나예요");
        }
        return type;
    }
}
