package com.dipsource.service.search.impl;

import com.dipsource.dto.demo.DemoSearchItemResponse;
import com.dipsource.dto.feed.FeedSearchItemResponse;
import com.dipsource.dto.post.PostSearchItemResponse;
import com.dipsource.dto.search.SearchAllResponse;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchPageResponse;
import com.dipsource.dto.search.SearchSection;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchType;
import com.dipsource.dto.user.UserSearchItemResponse;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.service.search.SearchService;
import com.dipsource.service.search.query.ParsedQuery;
import com.dipsource.service.search.query.SearchQueryParser;
import com.dipsource.service.search.query.TechTermDictionary;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 설정에 따라 검색 경로를 고르고, Elasticsearch가 실패하면 PostgreSQL 검색으로 대체한다.
 * 한 번 실패하면 일정 시간 Elasticsearch를 호출하지 않는다. 장애 중 요청마다 타임아웃을 기다리지 않기 위해서다.
 */
@Slf4j
@Primary
@Service
public class SearchRouter implements SearchService {

    private final SearchService postgres;
    private final ObjectProvider<EngineSearchService> engine;
    private final SearchProperties properties;
    private final MeterRegistry meterRegistry;
    private final SearchQueryParser queryParser;
    private final TechTermDictionary dictionary;
    private volatile long circuitOpenUntil = 0;

    public SearchRouter(@Qualifier("postgresSearchService") SearchService postgres,
                        ObjectProvider<EngineSearchService> engine,
                        SearchProperties properties,
                        MeterRegistry meterRegistry,
                        SearchQueryParser queryParser,
                        TechTermDictionary dictionary) {
        this.postgres = postgres;
        this.engine = engine;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.queryParser = queryParser;
        this.dictionary = dictionary;
    }

    @Override
    public SearchAllResponse searchAll(String query, SearchOperator operator, SearchSort sort, Long viewerId) {
        return route(query, service -> service.searchAll(query, operator, sort, viewerId),
                () -> postgres.searchAll(query, operator, sort, viewerId),
                SearchRouter::emptyAll);
    }

    @Override
    public SearchPageResponse<?> search(String query, SearchType type, SearchOperator operator, SearchSort sort,
                                        Pageable pageable, Long viewerId, String searchId) {
        return route(query, service -> service.search(query, type, operator, sort, pageable, viewerId, searchId),
                () -> postgres.search(query, type, operator, sort, pageable, viewerId, searchId),
                () -> new SearchPageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0,
                        null, null, null, null, null));
    }

    private static SearchAllResponse emptyAll() {
        SearchSection<PostSearchItemResponse> posts = new SearchSection<>(List.of(), 0);
        SearchSection<FeedSearchItemResponse> feeds = new SearchSection<>(List.of(), 0);
        SearchSection<DemoSearchItemResponse> demos = new SearchSection<>(List.of(), 0);
        SearchSection<UserSearchItemResponse> users = new SearchSection<>(List.of(), 0);
        return new SearchAllResponse(posts, feeds, demos, users, null, null, null);
    }

    /** PostgreSQL LIKE로는 한 글자가 부분 문자열로 전부 걸리므로 대체 경로에서는 빈 결과를 준다 */
    private <T> Supplier<T> degraded(ParsedQuery parsed, Supplier<T> fallback, Supplier<T> empty) {
        boolean singleCharacter = parsed.keywords().size() == 1 && dictionary.isSingleCharacterTerm(parsed.keywords().get(0));
        return singleCharacter ? empty : fallback;
    }

    private <T> T route(String query, Function<SearchService, T> primary, Supplier<T> fallback, Supplier<T> empty) {
        EngineSearchService service = engine.getIfAvailable();
        if (properties.getEngine() != SearchProperties.Engine.ELASTICSEARCH || service == null) {
            return fallback.get();
        }
        // 대체 경로를 타더라도 검색어 규칙은 같아야 한다
        Supplier<T> degraded = degraded(queryParser.parse(query), fallback, empty);
        if (System.currentTimeMillis() < circuitOpenUntil && properties.isFallbackToPostgres()) {
            meterRegistry.counter("search.fallback", "reason", "circuit_open").increment();
            return degraded.get();
        }
        try {
            return primary.apply(service);
        } catch (BusinessException e) {
            // 검색어 검증 실패 같은 사용자 오류는 대체 경로로 넘기지 않는다
            throw e;
        } catch (RuntimeException e) {
            if (!properties.isFallbackToPostgres()) {
                throw e;
            }
            log.warn("Elasticsearch 검색 실패, PostgreSQL로 대체합니다: {}", e.toString());
            circuitOpenUntil = System.currentTimeMillis() + properties.getCircuitOpenDuration().toMillis();
            meterRegistry.counter("search.fallback", "reason", "error").increment();
            return degraded.get();
        }
    }
}
