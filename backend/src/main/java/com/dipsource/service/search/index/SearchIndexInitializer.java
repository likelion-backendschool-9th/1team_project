package com.dipsource.service.search.index;

import com.dipsource.dto.search.SearchType;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.service.search.suggest.SuggestIndexService;
import com.dipsource.service.search.sync.SearchReindexService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 서버가 뜰 때 인덱스가 없으면 만든다. 검색 엔진이 죽어 있어도 서버 기동은 막지 않는다. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class SearchIndexInitializer implements ApplicationRunner {

    private final SearchIndexManager indexManager;
    private final SuggestIndexService suggestIndex;
    private final SearchReindexService reindexService;
    private final SearchProperties properties;

    /** 처음 띄울 때 인덱스가 비어 있으면 PostgreSQL 데이터로 채운다 */
    private void bootstrapIfEmpty() throws IOException {
        if (!properties.getElasticsearch().isBootstrapOnEmpty()) {
            return;
        }
        long documents = 0;
        for (SearchType type : SearchIndexManager.TYPES) {
            documents += indexManager.count(type);
        }
        if (documents == 0) {
            log.info("검색 인덱스가 비어 있어 초기 색인을 시작합니다");
            reindexService.startRebuild();
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        for (SearchType type : SearchIndexManager.TYPES) {
            try {
                indexManager.ensureIndex(type);
            } catch (IOException | RuntimeException e) {
                log.warn("검색 인덱스 준비 실패({}): {}", type, e.toString());
            }
        }
        try {
            suggestIndex.ensureIndex();
            bootstrapIfEmpty();
        } catch (IOException | RuntimeException e) {
            log.warn("검색어 사전 인덱스 준비 실패: {}", e.toString());
        }
    }
}
