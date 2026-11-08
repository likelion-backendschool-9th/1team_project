package com.dipsource.service.search.impl;

import com.dipsource.dto.search.SearchAllResponse;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchPageResponse;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchType;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.service.search.SearchService;
import io.micrometer.core.instrument.MeterRegistry;
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
    private volatile long circuitOpenUntil = 0;

    public SearchRouter(@Qualifier("postgresSearchService") SearchService postgres,
                        ObjectProvider<EngineSearchService> engine,
                        SearchProperties properties,
                        MeterRegistry meterRegistry) {
        this.postgres = postgres;
        this.engine = engine;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public SearchAllResponse searchAll(String query, SearchOperator operator, SearchSort sort, Long viewerId) {
        return route(service -> service.searchAll(query, operator, sort, viewerId),
                () -> postgres.searchAll(query, operator, sort, viewerId));
    }

    @Override
    public SearchPageResponse<?> search(String query, SearchType type, SearchOperator operator, SearchSort sort,
                                        Pageable pageable, Long viewerId) {
        return route(service -> service.search(query, type, operator, sort, pageable, viewerId),
                () -> postgres.search(query, type, operator, sort, pageable, viewerId));
    }

    private <T> T route(java.util.function.Function<SearchService, T> primary, Supplier<T> fallback) {
        EngineSearchService service = engine.getIfAvailable();
        if (properties.getEngine() != SearchProperties.Engine.ELASTICSEARCH || service == null) {
            return fallback.get();
        }
        if (System.currentTimeMillis() < circuitOpenUntil && properties.isFallbackToPostgres()) {
            meterRegistry.counter("search.fallback", "reason", "circuit_open").increment();
            return fallback.get();
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
            return fallback.get();
        }
    }
}
