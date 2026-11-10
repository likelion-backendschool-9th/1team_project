package com.dipsource.search.eval;

import com.dipsource.dto.search.SearchType;
import com.dipsource.search.SearchContainers;
import com.dipsource.service.search.index.SearchIndexManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Elasticsearch 프로필 공통. 말뭉치를 넣은 뒤 전체 재색인한다. */
abstract class AbstractEsEval extends AbstractSearchEval {

    @Autowired
    SearchIndexManager indexManager;
    @Autowired
    com.dipsource.service.search.suggest.SuggestIndexService suggestIndex;

    @DynamicPropertySource
    static void containers(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SearchContainers.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", SearchContainers.POSTGRES::getUsername);
        registry.add("spring.datasource.password", SearchContainers.POSTGRES::getPassword);
        registry.add("spring.elasticsearch.uris", SearchContainers::elasticsearchUri);
    }

    @Override
    void prepare(EvalCorpusLoader corpus) throws Exception {
        for (SearchType type : SearchIndexManager.TYPES) {
            indexManager.rebuild(type);
        }
        suggestIndex.rebuild();
    }
}
