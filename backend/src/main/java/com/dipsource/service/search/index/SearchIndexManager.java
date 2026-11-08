package com.dipsource.service.search.index;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.indices.GetAliasResponse;
import com.dipsource.dto.search.SearchType;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.service.search.index.SearchDocumentRepository.SourceRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 인덱스 생성과 Alias 전환. 검색은 항상 Alias로 읽으므로 새 버전 인덱스를 다 채운 뒤 Alias를 옮기면 운영 중에도 중단 없이 재색인된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class SearchIndexManager {

    private static final DateTimeFormatter VERSION = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    public static final List<SearchType> TYPES = List.of(SearchType.POST, SearchType.DEMO, SearchType.FEED, SearchType.USER);

    private final ElasticsearchClient client;
    private final SearchProperties properties;
    private final SearchIndexDefinitions definitions;
    private final SearchDocumentRepository documents;
    private final SearchDocumentFactory documentFactory;
    private final ObjectMapper objectMapper;

    public String alias(SearchType type) {
        return properties.getElasticsearch().getIndexPrefix() + "_" + type.name().toLowerCase(Locale.ROOT);
    }

    /** 대상의 Alias가 없으면 빈 인덱스를 만든다. 서버가 뜰 때 호출한다 */
    public void ensureIndex(SearchType type) throws IOException {
        if (!client.indices().existsAlias(a -> a.name(alias(type))).value()) {
            String index = createVersionedIndex(type);
            client.indices().updateAliases(u -> u.actions(a -> a.add(add -> add.index(index).alias(alias(type)))));
            log.info("검색 인덱스 생성: {} -> {}", alias(type), index);
        }
    }

    /**
     * 새 버전 인덱스를 처음부터 채우고 Alias를 원자적으로 옮긴 뒤 이전 인덱스를 지운다.
     *
     * @return 색인한 문서 수
     */
    public long rebuild(SearchType type) throws IOException {
        String index = createVersionedIndex(type);
        long count = 0;
        long afterId = 0;
        int bulkSize = properties.getElasticsearch().getBulkSize();
        while (true) {
            List<SourceRow> rows = documents.findAfter(type, afterId, bulkSize);
            if (rows.isEmpty()) {
                break;
            }
            bulkIndex(index, type, rows);
            count += rows.size();
            afterId = rows.get(rows.size() - 1).id();
        }
        client.indices().refresh(r -> r.index(index));
        swapAlias(type, index);
        return count;
    }

    private void bulkIndex(String index, SearchType type, List<SourceRow> rows) throws IOException {
        BulkRequest.Builder bulk = new BulkRequest.Builder();
        for (SourceRow row : rows) {
            Map<String, Object> doc = documentFactory.create(type, row.id(), row.values());
            bulk.operations(op -> op.index(i -> i.index(index).id(String.valueOf(row.id())).document(doc)));
        }
        BulkResponse response = client.bulk(bulk.build());
        if (response.errors()) {
            String reason = response.items().stream()
                    .filter(item -> item.error() != null)
                    .map(BulkResponseItem::error)
                    .map(error -> error.reason())
                    .findFirst().orElse("알 수 없음");
            throw new IOException("벌크 색인 실패: " + reason);
        }
    }

    private String createVersionedIndex(SearchType type) throws IOException {
        String index = alias(type) + "_" + LocalDateTime.now().format(VERSION);
        String body = toJson(definitions.indexBody(type));
        client.indices().create(c -> c.index(index).withJson(new StringReader(body)));
        return index;
    }

    private void swapAlias(SearchType type, String newIndex) throws IOException {
        String alias = alias(type);
        Set<String> previous = currentIndices(alias);
        List<co.elastic.clients.elasticsearch.indices.update_aliases.Action> actions = new ArrayList<>();
        for (String old : previous) {
            actions.add(co.elastic.clients.elasticsearch.indices.update_aliases.Action.of(
                    a -> a.remove(r -> r.index(old).alias(alias))));
        }
        actions.add(co.elastic.clients.elasticsearch.indices.update_aliases.Action.of(
                a -> a.add(add -> add.index(newIndex).alias(alias))));
        client.indices().updateAliases(u -> u.actions(actions));
        for (String old : previous) {
            client.indices().delete(d -> d.index(old));
        }
    }

    private Set<String> currentIndices(String alias) throws IOException {
        if (!client.indices().existsAlias(a -> a.name(alias)).value()) {
            return Set.of();
        }
        GetAliasResponse response = client.indices().getAlias(a -> a.name(alias));
        return response.result().keySet();
    }

    private String toJson(Map<String, Object> body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
