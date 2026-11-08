package com.dipsource.service.search.engine;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.service.search.index.SearchIndexManager;
import io.micrometer.core.annotation.Timed;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** {@link SearchPort}의 Elasticsearch 구현체. 문서 본문은 받지 않고 ID와 점수만 받는다. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class ElasticsearchSearchAdapter implements SearchPort {

    private final ElasticsearchClient client;
    private final ElasticsearchQueryFactory queryFactory;
    private final SearchIndexManager indexManager;

    @Override
    @Timed(value = "search.engine", description = "Elasticsearch 후보 검색 시간")
    public Result retrieve(Request request) {
        SearchRequest.Builder search = new SearchRequest.Builder()
                .index(indexManager.alias(request.type()))
                .query(queryFactory.build(request))
                .from(request.from())
                .size(request.size())
                .source(s -> s.fetch(false))
                .trackTotalHits(t -> t.enabled(true));
        if (request.sort() == SearchSort.LATEST) {
            search.sort(s -> s.field(f -> f.field("created_at").order(SortOrder.Desc)));
        } else {
            search.sort(s -> s.score(o -> o.order(SortOrder.Desc)));
            search.sort(s -> s.field(f -> f.field("created_at").order(SortOrder.Desc)));
        }
        // ID를 마지막 정렬 기준으로 삼아 점수가 같아도 순서가 고정된다
        search.sort(s -> s.field(f -> f.field("doc_id").order(SortOrder.Desc)));

        try {
            SearchResponse<JsonData> response = client.search(search.build(), JsonData.class);
            List<Hit> hits = response.hits().hits().stream()
                    .map(hit -> new Hit(
                            Long.parseLong(hit.id()),
                            hit.score() == null ? 0 : hit.score(),
                            hit.matchedQueries().contains(ElasticsearchQueryFactory.EXACT_QUERY_NAME)))
                    .toList();
            long total = response.hits().total() == null ? hits.size() : response.hits().total().value();
            return new Result(hits, total);
        } catch (IOException e) {
            throw new UncheckedIOException("Elasticsearch 검색 실패", e);
        }
    }
}
