package com.dipsource.service.search.index;

import com.dipsource.dto.search.SearchType;
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

    @Override
    public void run(ApplicationArguments args) {
        for (SearchType type : SearchIndexManager.TYPES) {
            try {
                indexManager.ensureIndex(type);
            } catch (IOException | RuntimeException e) {
                log.warn("검색 인덱스 준비 실패({}): {}", type, e.toString());
            }
        }
    }
}
