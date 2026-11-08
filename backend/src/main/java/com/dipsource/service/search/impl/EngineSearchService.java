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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.dipsource.service.search.engine.SearchPort;
import com.dipsource.service.search.query.ParsedQuery;
import com.dipsource.service.search.ranking.SearchRanker;
import com.dipsource.service.search.query.SearchQueryParser;
import com.dipsource.service.user.UserSearchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Elasticsearch 검색 경로: 후보 검색 → PostgreSQL 권한·신호 확인 → 재정렬 → 응답 조립.
 * 인덱스가 늦어도 삭제·비공개 문서는 신호 조회에서 빠져서 노출되지 않는다.
 */
@Service
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class EngineSearchService implements SearchService {

    static final int MAX_PAGE_SIZE = 50;
    static final int PREVIEW_SIZE = 5;
    /** Elasticsearch 기본 max_result_window */
    static final int MAX_RESULT_WINDOW = 10_000;

    private final SearchPort searchPort;
    private final SearchQueryParser queryParser;
    private final SearchRanker ranker;
    private final SearchProperties properties;
    private final PostSearchService postSearchService;
    private final FeedSearchService feedSearchService;
    private final DemoSearchService demoSearchService;
    private final UserSearchService userSearchService;
    private final Cache<String, RankedWindow> windowCache;

    public EngineSearchService(SearchPort searchPort, SearchQueryParser queryParser, SearchRanker ranker,
                               SearchProperties properties, PostSearchService postSearchService,
                               FeedSearchService feedSearchService, DemoSearchService demoSearchService,
                               UserSearchService userSearchService) {
        this.searchPort = searchPort;
        this.queryParser = queryParser;
        this.ranker = ranker;
        this.properties = properties;
        this.postSearchService = postSearchService;
        this.feedSearchService = feedSearchService;
        this.demoSearchService = demoSearchService;
        this.userSearchService = userSearchService;
        this.windowCache = Caffeine.newBuilder()
                .expireAfterWrite(properties.getRanking().getWindowCacheTtl())
                .maximumSize(properties.getRanking().getWindowCacheSize())
                .build();
    }

    private record RankedPage(List<Long> ids, long total) {
    }

    /** 재정렬을 마친 상위 후보. truncated면 window 뒤에 문서가 더 있다 */
    private record RankedWindow(List<Long> ids, long total, boolean truncated) {
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

    /**
     * 페이지 조회. 재정렬은 현재 페이지가 아니라 상위 후보(window) 전체에 한 번 적용하고 그 결과를 잘라 쓴다.
     * 페이지마다 따로 재정렬하면 페이지 경계에서 중복·누락이 생기기 때문이다.
     * window를 넘는 페이지는 Elasticsearch 순서를 그대로 이어서 쓴다. window는 항상 BM25 상위 문서라 겹치지 않는다.
     */
    private RankedPage rank(SearchType type, ParsedQuery parsed, SearchOperator operator, SearchSort sort,
                            Pageable page) {
        int offset = (int) page.getOffset();
        int size = page.getPageSize();
        if (offset + size > MAX_RESULT_WINDOW) {
            return new RankedPage(List.of(), 0);
        }
        if (sort == SearchSort.LATEST) {
            SearchPort.Result result = retrieve(type, parsed, operator, sort, offset, size);
            return new RankedPage(visibleIds(type, result.hits()), result.total());
        }

        RankedWindow ranked = window(type, parsed, operator);
        List<Long> ids = new ArrayList<>();
        if (offset < ranked.ids().size()) {
            ids.addAll(ranked.ids().subList(offset, Math.min(offset + size, ranked.ids().size())));
        }
        // 인덱스가 늦어서 캐시 뒤에 삭제된 문서가 있을 수 있어서 돌려주기 전에 한 번 더 확인한다
        Set<Long> visible = signals(type, ids).keySet();
        List<Long> result = new ArrayList<>(ids.stream().filter(visible::contains).toList());
        int missing = size - ids.size();
        if (missing > 0 && ranked.truncated()) {
            int beyond = properties.getRanking().getWindow() + Math.max(0, offset - ranked.ids().size());
            result.addAll(visibleIds(type, retrieve(type, parsed, operator, sort, beyond, missing).hits()));
        }
        return new RankedPage(result, ranked.total());
    }

    private RankedWindow window(SearchType type, ParsedQuery parsed, SearchOperator operator) {
        String key = String.join("|", type.name(), operator.name(), parsed.phrase(), String.join(",", parsed.hashtags()));
        return windowCache.get(key, ignored -> computeWindow(type, parsed, operator));
    }

    private RankedWindow computeWindow(SearchType type, ParsedQuery parsed, SearchOperator operator) {
        int window = properties.getRanking().getWindow();
        SearchPort.Result result = retrieve(type, parsed, operator, SearchSort.RELEVANCE, 0, window);
        Map<Long, SearchSignal> visible = signals(type, result.hits().stream().map(SearchPort.Hit::id).toList());
        List<SearchRanker.Candidate> candidates = result.hits().stream()
                .filter(hit -> visible.containsKey(hit.id()))
                .map(hit -> SearchRanker.Candidate.from(hit, visible.get(hit.id())))
                .toList();
        long removed = result.hits().size() - candidates.size();
        List<Long> ids = ranker.rank(type, candidates);
        return new RankedWindow(ids, Math.max(0, result.total() - removed), result.total() > result.hits().size());
    }

    private SearchPort.Result retrieve(SearchType type, ParsedQuery parsed, SearchOperator operator, SearchSort sort,
                                       int from, int size) {
        return searchPort.retrieve(new SearchPort.Request(type, parsed, operator, sort, from, size, Set.of(), Set.of()));
    }

    private List<Long> visibleIds(SearchType type, List<SearchPort.Hit> hits) {
        List<Long> ids = hits.stream().map(SearchPort.Hit::id).toList();
        Set<Long> visible = signals(type, ids).keySet();
        return ids.stream().filter(visible::contains).toList();
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
