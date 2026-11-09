package com.dipsource.service.search.index;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.VersionType;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
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
import java.util.HashMap;
import java.util.HashSet;
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
        } else {
            // 이전 버전으로 만든 인덱스에도 새 필드를 쓸 수 있게 한다. 값은 정합성 점검이 채운다
            client.indices().putMapping(p -> p.index(alias(type))
                    .properties("content_hash", f -> f.keyword(k -> k.index(false))));
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

    /**
     * 동기화 결과.
     *
     * @param failedIds      문서 자체가 거절된 것(시도 횟수를 쓴다)
     * @param unavailableIds 검색 엔진·인덱스를 쓸 수 없어서 못 쓴 것(시도 횟수를 쓰지 않는다)
     */
    public record SyncResult(List<Long> failedIds, List<Long> unavailableIds) {
    }

    /**
     * 문서들을 PostgreSQL의 현재 상태로 맞춘다. 공개 상태면 색인하고, 아니면 지운다.
     * 대기열 ID를 외부 버전으로 써서 더 오래된 처리가 늦게 도착해도 새 내용을 덮어쓰지 못한다.
     *
     * @param latest 문서 ID → 그 문서의 가장 최근 대기열 ID
     */
    public SyncResult sync(SearchType type, Map<Long, Long> latest) throws IOException {
        String alias = alias(type);
        Map<Long, SourceRow> rows = new HashMap<>();
        documents.findByIds(type, latest.keySet()).forEach(row -> rows.put(row.id(), row));

        BulkRequest.Builder bulk = new BulkRequest.Builder();
        latest.forEach((id, version) -> {
            SourceRow row = rows.get(id);
            if (row == null) {
                bulk.operations(op -> op.delete(d -> d.index(alias).id(String.valueOf(id))
                        .version(version).versionType(VersionType.ExternalGte)));
            } else {
                Map<String, Object> doc = documentFactory.create(type, id, row.values());
                bulk.operations(op -> op.index(i -> i.index(alias).id(String.valueOf(id)).document(doc)
                        .version(version).versionType(VersionType.ExternalGte)));
            }
        });
        BulkResponse response = client.bulk(bulk.build());
        List<Long> failed = new ArrayList<>();
        List<Long> unavailable = new ArrayList<>();
        for (BulkResponseItem item : response.items()) {
            // 409는 더 최신 버전이 이미 반영됐다는 뜻이고, 404는 이미 지워졌다는 뜻이라 성공으로 본다
            if (item.error() == null || item.status() == 409 || item.status() == 404) {
                continue;
            }
            (isUnavailable(item.status(), item.error().type()) ? unavailable : failed).add(Long.parseLong(item.id()));
        }
        if (unavailable.size() < latest.size()) {
            client.indices().refresh(r -> r.index(alias));
        }
        return new SyncResult(failed, unavailable);
    }

    /** 문서가 아니라 검색 엔진·인덱스 상태 때문에 실패했는지 */
    static boolean isUnavailable(int status, String errorType) {
        return status >= 500 || status == 429 || "index_closed_exception".equals(errorType)
                || "cluster_block_exception".equals(errorType);
    }

    /** 원본과 다른 문서의 ID. 인덱스에 없는 문서, 지워야 할 문서, 내용이 뒤처진 문서를 모두 찾는다 */
    public List<Long> findDifferences(SearchType type) throws IOException {
        Map<Long, String> source = new HashMap<>();
        long afterId = 0;
        int bulkSize = properties.getElasticsearch().getBulkSize();
        while (true) {
            List<SourceRow> rows = documents.findAfter(type, afterId, bulkSize);
            if (rows.isEmpty()) {
                break;
            }
            for (SourceRow row : rows) {
                source.put(row.id(), (String) documentFactory.create(type, row.id(), row.values()).get("content_hash"));
            }
            afterId = rows.get(rows.size() - 1).id();
        }
        Map<Long, String> indexed = indexedHashes(type);
        List<Long> differences = new ArrayList<>();
        source.forEach((id, hash) -> {
            if (!hash.equals(indexed.get(id))) {
                differences.add(id);
            }
        });
        indexed.keySet().stream().filter(id -> !source.containsKey(id)).forEach(differences::add);
        return differences;
    }

    private Map<Long, String> indexedHashes(SearchType type) throws IOException {
        Map<Long, String> hashes = new HashMap<>();
        Long after = null;
        while (true) {
            final Long cursor = after;
            SearchResponse<Map> response = client.search(s -> {
                s.index(alias(type)).size(5000).source(c -> c.filter(f -> f.includes("content_hash")))
                        .sort(o -> o.field(f -> f.field("doc_id").order(SortOrder.Asc)));
                if (cursor != null) {
                    s.searchAfter(FieldValue.of(cursor));
                }
                return s;
            }, Map.class);
            List<Hit<Map>> hits = response.hits().hits();
            if (hits.isEmpty()) {
                return hashes;
            }
            hits.forEach(hit -> hashes.put(Long.parseLong(hit.id()), hit.source() == null ? null : (String) hit.source().get("content_hash")));
            after = Long.parseLong(hits.get(hits.size() - 1).id());
        }
    }

    /** 인덱스에 있는 문서 ID 전부. 원본과 비교할 때 쓴다 */
    public Set<Long> indexedIds(SearchType type) throws IOException {
        Set<Long> ids = new HashSet<>();
        Long after = null;
        while (true) {
            final Long cursor = after;
            SearchResponse<Void> response = client.search(s -> {
                s.index(alias(type)).size(5000).source(c -> c.fetch(false)).sort(o -> o.field(f -> f.field("doc_id").order(SortOrder.Asc)));
                if (cursor != null) {
                    s.searchAfter(FieldValue.of(cursor));
                }
                return s;
            }, Void.class);
            List<Hit<Void>> hits = response.hits().hits();
            if (hits.isEmpty()) {
                return ids;
            }
            hits.forEach(hit -> ids.add(Long.parseLong(hit.id())));
            after = Long.parseLong(hits.get(hits.size() - 1).id());
        }
    }

    public long count(SearchType type) throws IOException {
        return client.count(c -> c.index(alias(type))).count();
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
        // 이전 버전과, 재색인이 중간에 끊겨 Alias 없이 남은 인덱스를 함께 지운다
        for (String old : client.indices().get(g -> g.index(alias + "_*")).result().keySet()) {
            if (!old.equals(newIndex)) {
                client.indices().delete(d -> d.index(old));
            }
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
