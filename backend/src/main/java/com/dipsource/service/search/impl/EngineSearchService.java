package com.dipsource.service.search.impl;

import com.dipsource.dto.demo.DemoSearchItemResponse;
import com.dipsource.dto.feed.FeedSearchItemResponse;
import com.dipsource.dto.post.PostSearchItemResponse;
import com.dipsource.dto.search.SearchAllResponse;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchPageResponse;
import com.dipsource.dto.search.SearchSection;
import com.dipsource.dto.search.SearchSignal;
import com.dipsource.dto.search.SearchSuggestion;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchType;
import com.dipsource.dto.user.UserSearchItemResponse;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.demo.DemoSearchService;
import com.dipsource.service.feed.FeedSearchService;
import com.dipsource.service.post.PostSearchService;
import com.dipsource.service.search.SearchService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import com.dipsource.service.search.engine.SearchPort;
import com.dipsource.service.search.personal.SearchPersonalizationService;
import com.dipsource.service.search.query.ParsedQuery;
import com.dipsource.service.search.ranking.SearchRanker;
import com.dipsource.service.search.suggest.SearchSuggester;
import com.dipsource.service.search.query.SearchQueryParser;
import com.dipsource.service.search.query.TechTermDictionary;
import com.dipsource.service.user.UserSearchService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Elasticsearch 검색 경로: 후보 검색 → PostgreSQL 권한·신호 확인 → 재정렬 → 응답 조립.
 * 인덱스가 늦어도 삭제·비공개 문서는 신호 조회에서 빠져서 노출되지 않는다.
 */
@Slf4j
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
    private final SearchSuggester suggester;
    private final SearchPersonalizationService personalization;
    private final SearchProperties properties;
    private final TechTermDictionary dictionary;
    private final PostSearchService postSearchService;
    private final FeedSearchService feedSearchService;
    private final DemoSearchService demoSearchService;
    private final UserSearchService userSearchService;
    private final Cache<String, RankedWindow> windowCache;
    private final Cache<String, Snapshot> snapshots;
    private final MeterRegistry meterRegistry;

    public EngineSearchService(SearchPort searchPort, SearchQueryParser queryParser, SearchRanker ranker,
                               SearchSuggester suggester, SearchPersonalizationService personalization,
                               SearchProperties properties, TechTermDictionary dictionary, PostSearchService postSearchService,
                               FeedSearchService feedSearchService, DemoSearchService demoSearchService,
                               UserSearchService userSearchService, MeterRegistry meterRegistry) {
        this.searchPort = searchPort;
        this.queryParser = queryParser;
        this.ranker = ranker;
        this.suggester = suggester;
        this.personalization = personalization;
        this.properties = properties;
        this.dictionary = dictionary;
        this.postSearchService = postSearchService;
        this.feedSearchService = feedSearchService;
        this.demoSearchService = demoSearchService;
        this.userSearchService = userSearchService;
        this.meterRegistry = meterRegistry;
        this.windowCache = Caffeine.newBuilder()
                .expireAfterWrite(properties.getRanking().getWindowCacheTtl())
                .maximumSize(properties.getRanking().getWindowCacheSize())
                .build();
        this.snapshots = Caffeine.newBuilder()
                .expireAfterAccess(properties.getRanking().getSnapshotIdle())
                .maximumSize(properties.getRanking().getWindowCacheSize())
                .build();
    }

    private record RankedPage(List<Long> ids, long total, String searchId) {
    }

    /**
     * 재정렬을 마친 상위 후보.
     *
     * @param truncated     window 뒤에 문서가 더 있다
     * @param createdBefore 이 window를 만든 시각. 이어지는 페이지는 이후에 생긴 글을 빼서 순서가 밀리지 않게 한다
     */
    private record RankedWindow(List<Long> ids, long total, boolean truncated, LocalDateTime createdBefore) {
    }

    /** 한 번의 검색 탐색이 끝까지 보는 순서. key로 다른 사용자·검색어의 searchId를 쓰지 못하게 한다 */
    private record Snapshot(String key, RankedWindow window) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public SearchAllResponse searchAll(String query, SearchOperator operator, SearchSort sort, Long viewerId) {
        ParsedQuery parsed = queryParser.parse(query);
        Set<Long> blocked = personalization.blockedUsers(viewerId);
        SearchSection<PostSearchItemResponse> posts = (SearchSection<PostSearchItemResponse>) section(SearchType.POST, parsed, operator, sort, viewerId, blocked);
        SearchSection<FeedSearchItemResponse> feeds = (SearchSection<FeedSearchItemResponse>) section(SearchType.FEED, parsed, operator, sort, viewerId, blocked);
        SearchSection<DemoSearchItemResponse> demos = (SearchSection<DemoSearchItemResponse>) section(SearchType.DEMO, parsed, operator, sort, viewerId, blocked);
        SearchSection<UserSearchItemResponse> users = (SearchSection<UserSearchItemResponse>) section(SearchType.USER, parsed, operator, sort, viewerId, blocked);
        boolean anyRelaxed = Stream.of(posts, feeds, demos, users).anyMatch(section -> section.relaxedMatch() != null);
        SearchSuggestion suggestion = suggestion(parsed, anyRelaxed, SearchType.POST, SearchType.DEMO, SearchType.FEED, SearchType.USER);
        return new SearchAllResponse(posts, feeds, demos, users, null,
                suggestion == null ? null : suggestion.query(),
                suggestion == null ? null : suggestion.type());
    }

    /** 대상 하나의 미리보기. 정확히 일치하는 결과가 없는 대상만 검색어로 시작하는 단어로 다시 찾는다 */
    private SearchSection<?> section(SearchType type, ParsedQuery parsed, SearchOperator operator, SearchSort sort,
                                     Long viewerId, Set<Long> blocked) {
        Pageable preview = PageRequest.of(0, PREVIEW_SIZE);
        RankedPage page = rank(type, parsed, operator, sort, preview, viewerId, blocked, null, false);
        if (page.total() == 0 && canExpandToPrefix(parsed, operator)) {
            RankedPage expanded = rank(type, parsed.asPrefix(), operator, sort, preview, viewerId, blocked, null, false);
            if (expanded.total() > 0) {
                return new SearchSection<>(items(type, expanded.ids()), expanded.total(), Boolean.TRUE);
            }
        }
        return new SearchSection<>(items(type, page.ids()), page.total());
    }

    /** op를 보내지 않은 한 단어 검색만 넓힌다. 명시적 and/or, 해시태그, 허용된 한 글자 용어의 의미는 바꾸지 않는다 */
    private boolean canExpandToPrefix(ParsedQuery parsed, SearchOperator operator) {
        return operator == SearchOperator.AUTO && parsed.hashtags().isEmpty() && parsed.keywords().size() == 1
                && !dictionary.isSingleCharacterTerm(parsed.keywords().get(0));
    }

    @Override
    public SearchPageResponse<?> search(String query, SearchType type, SearchOperator operator, SearchSort sort,
                                        Pageable pageable, Long viewerId, String searchId) {
        ParsedQuery parsed = queryParser.parse(query);
        Pageable page = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE));
        Set<Long> blocked = personalization.blockedUsers(viewerId);
        RankedPage ranked = rank(type, parsed, operator, sort, page, viewerId, blocked, searchId, true);
        Boolean relaxed = null;
        boolean expanded = false;
        // 명시적 and는 절대 넓히지 않는다. op를 보내지 않았고 모든 단어가 일치하는 결과가 없을 때만 일부 일치를 보여 준다
        if (ranked.total() == 0 && operator == SearchOperator.AUTO && parsed.keywords().size() > 1) {
            RankedPage partial = rank(type, parsed, SearchOperator.OR, sort, page, viewerId, blocked, searchId, true);
            if (partial.total() > 0) {
                ranked = partial;
                relaxed = Boolean.TRUE;
            }
        } else if (ranked.total() == 0 && canExpandToPrefix(parsed, operator)) {
            RankedPage partial = rank(type, parsed.asPrefix(), operator, sort, page, viewerId, blocked, searchId, true);
            if (partial.total() > 0) {
                ranked = partial;
                relaxed = Boolean.TRUE;
                expanded = true;
            }
        }
        int totalPages = page.getPageSize() == 0 ? 0 : (int) Math.ceil((double) ranked.total() / page.getPageSize());
        SearchSuggestion suggestion = suggestion(parsed, expanded, type);
        return new SearchPageResponse<>(items(type, ranked.ids()), page.getPageNumber(), page.getPageSize(),
                ranked.total(), totalPages, null,
                suggestion == null ? null : suggestion.query(),
                suggestion == null ? null : suggestion.type(),
                relaxed,
                ranked.searchId());
    }

    /** 제안 검색어. 제안어로 실제 결과가 있을 때만 준다 */
    private SearchSuggestion suggestion(ParsedQuery parsed, boolean expanded, SearchType... types) {
        try {
            Optional<SearchSuggestion> candidate = suggester.suggest(parsed);
            if (candidate.isEmpty() && expanded) {
                candidate = suggester.suggestCompletion(parsed);
            }
            if (candidate.isEmpty()) {
                return null;
            }
            ParsedQuery suggested = queryParser.parse(candidate.get().query());
            for (SearchType type : types) {
                if (retrieve(type, suggested, SearchOperator.AUTO, SearchSort.RELEVANCE, 0, 1, Set.of(), Set.of(), null).total() > 0) {
                    return candidate.get();
                }
            }
            return null;
        } catch (RuntimeException e) {
            // 제안은 부가 기능이라 실패해도 검색 결과는 그대로 준다
            log.warn("검색어 제안 실패: {}", e.toString());
            return null;
        }
    }

    /**
     * 페이지 조회. 재정렬은 현재 페이지가 아니라 상위 후보(window) 전체에 한 번 적용하고 그 결과를 잘라 쓴다.
     * 페이지마다 따로 재정렬하면 페이지 경계에서 중복·누락이 생기기 때문이다.
     * window를 넘는 페이지는 window 문서를 뺀 나머지를 Elasticsearch 순서로 이어서 쓴다.
     * 차단·숨김·삭제는 캐시와 상관없이 응답 직전에 다시 걸러서, window가 만들어진 뒤의 변경도 바로 반영된다.
     *
     * @param searchId 앞 페이지에서 받은 탐색 ID. 있으면 그때의 순서를 그대로 쓴다
     * @param paging   이어지는 페이지를 볼 수 있는 조회인지. 전체 탭 미리보기는 탐색 ID를 만들지 않는다
     */
    private RankedPage rank(SearchType type, ParsedQuery parsed, SearchOperator operator, SearchSort sort,
                            Pageable page, Long viewerId, Set<Long> blocked, String searchId, boolean paging) {
        int offset = (int) page.getOffset();
        int size = page.getPageSize();
        if (offset + size > MAX_RESULT_WINDOW) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "검색 결과는 " + MAX_RESULT_WINDOW + "건까지 볼 수 있어요");
        }
        Set<Long> hidden = personalization.hiddenIds(viewerId, type);
        String key = String.join("|", viewerKey(viewerId), type.name(), operator.name(), sort.name(), parsed.phrase(),
                String.join(",", parsed.hashtags()), String.valueOf(parsed.prefix()));
        Snapshot existing = searchId == null ? null : snapshots.getIfPresent(searchId);
        boolean reuse = existing != null && existing.key().equals(key);
        RankedWindow window = reuse ? existing.window() : window(type, parsed, operator, sort, viewerId, hidden, blocked, key, offset == 0);
        String id = searchId;
        if (!reuse && paging) {
            id = UUID.randomUUID().toString();
            snapshots.put(id, new Snapshot(key, window));
        }

        if (sort == SearchSort.LATEST) {
            SearchPort.Result result = retrieve(type, parsed, operator, sort, offset, size, hidden, blocked, window.createdBefore());
            List<Long> ids = result.hits().stream().map(SearchPort.Hit::id).toList();
            return new RankedPage(servable(type, ids, hidden, blocked), result.total(), id);
        }

        // 삭제·차단으로 빠진 문서를 제외한 목록을 잘라서 페이지 크기를 유지한다
        List<Long> visible = servable(type, window.ids(), hidden, blocked);
        List<Long> result = new ArrayList<>();
        if (offset < visible.size()) {
            result.addAll(visible.subList(offset, Math.min(offset + size, visible.size())));
        }
        int missing = size - result.size();
        if (missing > 0 && window.truncated()) {
            Set<Long> excluded = new HashSet<>(hidden);
            excluded.addAll(window.ids());
            int from = Math.max(0, offset - visible.size());
            List<Long> beyond = retrieve(type, parsed, operator, sort, from, missing, excluded, blocked, window.createdBefore())
                    .hits().stream().map(SearchPort.Hit::id).toList();
            result.addAll(servable(type, beyond, hidden, blocked));
        }
        return new RankedPage(result, Math.max(0, window.total() - (window.ids().size() - visible.size())), id);
    }

    private RankedWindow window(SearchType type, ParsedQuery parsed, SearchOperator operator, SearchSort sort, Long viewerId,
                                Set<Long> hidden, Set<Long> blocked, String key, boolean firstPage) {
        if (sort == SearchSort.LATEST) {
            // 최신순은 재정렬이 없어서 기준 시각만 고정한다
            return new RankedWindow(List.of(), 0, true, LocalDateTime.now());
        }
        if (firstPage) {
            // 새 검색은 항상 최신 결과로 시작한다
            RankedWindow fresh = computeWindow(type, parsed, operator, viewerId, hidden, blocked);
            windowCache.put(key, fresh);
            return fresh;
        }
        return windowCache.get(key, ignored -> computeWindow(type, parsed, operator, viewerId, hidden, blocked));
    }

    private static String viewerKey(Long viewerId) {
        return viewerId == null ? "anon" : viewerId.toString();
    }

    /** 숨김·차단을 바꿨을 때 그 사용자의 재정렬 결과를 버린다 */
    public void invalidateViewer(Long viewerId) {
        String prefix = viewerKey(viewerId) + "|";
        windowCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
    }

    private RankedWindow computeWindow(SearchType type, ParsedQuery parsed, SearchOperator operator, Long viewerId,
                                       Set<Long> hidden, Set<Long> blocked) {
        int window = properties.getRanking().getWindow();
        LocalDateTime createdBefore = LocalDateTime.now();
        SearchPort.Result result = retrieve(type, parsed, operator, SearchSort.RELEVANCE, 0, window, hidden, blocked, null);
        List<Long> hitIds = result.hits().stream().map(SearchPort.Hit::id).toList();
        Map<Long, SearchSignal> visible = signals(type, hitIds);
        Map<Long, Double> personalBoosts = personalization.personalBoosts(viewerId, type, parsed, visible.keySet());
        List<SearchRanker.Candidate> candidates = result.hits().stream()
                .filter(hit -> visible.containsKey(hit.id()))
                .map(hit -> SearchRanker.Candidate.from(hit, visible.get(hit.id()))
                        .withPersonalBoost(personalBoosts.getOrDefault(hit.id(), 0.0)))
                .toList();
        long removed = result.hits().size() - candidates.size();
        List<Long> ids = meterRegistry.timer("search.rerank").record(() -> ranker.rank(type, candidates));
        return new RankedWindow(ids, Math.max(0, result.total() - removed), result.total() > result.hits().size(), createdBefore);
    }

    private SearchPort.Result retrieve(SearchType type, ParsedQuery parsed, SearchOperator operator, SearchSort sort,
                                       int from, int size, Set<Long> hidden, Set<Long> blocked, LocalDateTime createdBefore) {
        return searchPort.retrieve(new SearchPort.Request(type, parsed, operator, sort, from, size, hidden, blocked, createdBefore));
    }

    /** 지금 보여 줄 수 있는 문서만 순서대로 남긴다. 공개 상태, 숨김, 차단을 PostgreSQL 현재 값으로 확인한다 */
    private List<Long> servable(SearchType type, List<Long> ids, Set<Long> hidden, Set<Long> blocked) {
        Map<Long, SearchSignal> visible = signals(type, ids);
        return ids.stream()
                .filter(id -> visible.containsKey(id) && !hidden.contains(id) && !blocked.contains(visible.get(id).getAuthorId()))
                .toList();
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
