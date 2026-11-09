package com.dipsource.service.search.impl;

import com.dipsource.dto.search.AutocompleteResponse;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.service.search.AutocompleteService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/** 자동완성 경로 선택. Elasticsearch가 실패하면 PostgreSQL 자동완성으로 대체한다. */
@Slf4j
@Primary
@Service
public class AutocompleteRouter implements AutocompleteService {

    private final AutocompleteService postgres;
    private final ObjectProvider<AutocompleteService> engine;
    private final SearchProperties properties;

    public AutocompleteRouter(@Qualifier("postgresAutocompleteService") AutocompleteService postgres,
                              @Qualifier("engineAutocompleteService") ObjectProvider<AutocompleteService> engine,
                              SearchProperties properties) {
        this.postgres = postgres;
        this.engine = engine;
        this.properties = properties;
    }

    @Override
    public List<AutocompleteResponse> autocomplete(String query) {
        AutocompleteService service = engine.getIfAvailable();
        if (properties.getEngine() != SearchProperties.Engine.ELASTICSEARCH || service == null) {
            return postgres.autocomplete(query);
        }
        try {
            return service.autocomplete(query);
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            if (!properties.isFallbackToPostgres()) {
                throw e;
            }
            log.warn("Elasticsearch 자동완성 실패, PostgreSQL로 대체합니다: {}", e.toString());
            return postgres.autocomplete(query);
        }
    }
}
