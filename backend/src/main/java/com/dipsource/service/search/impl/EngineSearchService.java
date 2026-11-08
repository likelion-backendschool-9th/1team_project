package com.dipsource.service.search.impl;

import com.dipsource.dto.demo.DemoSearchItemResponse;
import com.dipsource.dto.feed.FeedSearchItemResponse;
import com.dipsource.dto.post.PostSearchItemResponse;
import com.dipsource.dto.search.SearchAllResponse;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchPageResponse;
import com.dipsource.dto.search.SearchSection;
import com.dipsource.dto.search.SearchSignal;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchType;
import com.dipsource.dto.user.UserSearchItemResponse;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.service.demo.DemoSearchService;
import com.dipsource.service.feed.FeedSearchService;
import com.dipsource.service.post.PostSearchService;
import com.dipsource.service.search.SearchService;
import com.dipsource.service.search.engine.SearchPort;
import com.dipsource.service.search.query.ParsedQuery;
import com.dipsource.service.search.query.SearchQueryParser;
import com.dipsource.service.user.UserSearchService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Elasticsearch 검색 경로: 후보 검색 → PostgreSQL 권한·신호 확인 → 재정렬 → 응답 조립.
 * 인덱스가 늦어도 삭제·비공개 문서는 신호 조회에서 빠져서 노출되지 않는다.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class EngineSearchService implements SearchService {

    static final int MAX_PAGE_SIZE = 50;
    static final int PREVIEW_SIZE = 5;
    /** Elasticsearch 기본 max_result_window */
    static final int MAX_RESULT_WINDOW = 10_000;

    private final SearchPort searchPort;
    private final SearchQueryParser queryParser;
    private final SearchProperties properties;
    private final PostSearchService postSearchService;
    private final FeedSearchService feedSearchService;
    private final DemoSearchService demoSearchService;
    private final UserSearchService userSearchService;

    private record RankedPage(List<Long> ids, long total) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public SearchAllResponse searchAll(String query, SearchOperator operator, SearchSort sort, Long viewerId) {
        ParsedQuery parsed = queryParser.parse(query);
        return new SearchAllResponse(
                (SearchSection<PostSearchItemResponse>) section(SearchType.POST, parsed, operator, sort),
                (SearchSection<FeedSearchItemResponse>) section(SearchType.FEED, parsed, operator, sort),
                (SearchSection<DemoSearchItemResponse>) section(SearchType.DEMO, parsed, operator, sort),
                (SearchSection<UserSearchItemResponse>) section(SearchType.USER, parsed, operator, sort),
                null,
                null);
    }

    private SearchSection<?> section(SearchType type, ParsedQuery parsed, SearchOperator operator, SearchSort sort) {
        RankedPage page = rank(type, parsed, operator, sort, PageRequest.of(0, PREVIEW_SIZE));
        return new SearchSection<>(items(type, page.ids()), page.total());
    }

    @Override
    public SearchPageResponse<?> search(String query, SearchType type, SearchOperator operator, SearchSort sort,
                                        Pageable pageable, Long viewerId) {
        ParsedQuery parsed = queryParser.parse(query);
        Pageable page = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE));
        RankedPage ranked = rank(type, parsed, operator, sort, page);
        int totalPages = page.getPageSize() == 0 ? 0 : (int) Math.ceil((double) ranked.total() / page.getPageSize());
        return new SearchPageResponse<>(items(type, ranked.ids()), page.getPageNumber(), page.getPageSize(),
                ranked.total(), totalPages, null, null);
    }

    private RankedPage rank(SearchType type, ParsedQuery parsed, SearchOperator operator, SearchSort sort, Pageable page) {
        int offset = (int) page.getOffset();
        if (offset + page.getPageSize() > MAX_RESULT_WINDOW) {
            return new RankedPage(List.of(), 0);
        }
        SearchPort.Result result = searchPort.retrieve(new SearchPort.Request(type, parsed, operator, sort, offset,
                page.getPageSize(), Set.of(), Set.of()));
        List<Long> ids = result.hits().stream().map(SearchPort.Hit::id).toList();
        Map<Long, SearchSignal> visible = signals(type, ids);
        List<Long> filtered = ids.stream().filter(visible::containsKey).toList();
        return new RankedPage(filtered, result.total());
    }

    private Map<Long, SearchSignal> signals(SearchType type, List<Long> ids) {
        List<SearchSignal> signals = switch (type) {
            case POST -> postSearchService.findSignals(ids);
            case DEMO -> demoSearchService.findSignals(ids);
            case FEED -> feedSearchService.findSignals(ids);
            case USER -> userSearchService.findSignals(ids);
            case ALL -> throw new IllegalArgumentException("ALL은 개별 대상이 아니다");
        };
        return signals.stream().collect(Collectors.toMap(SearchSignal::getId, Function.identity()));
    }

    private List<?> items(SearchType type, List<Long> ids) {
        return switch (type) {
            case POST -> postSearchService.findItems(ids);
            case DEMO -> demoSearchService.findItems(ids);
            case FEED -> feedSearchService.findItems(ids);
            case USER -> userSearchService.findItems(ids);
            case ALL -> throw new IllegalArgumentException("ALL은 개별 대상이 아니다");
        };
    }
}
