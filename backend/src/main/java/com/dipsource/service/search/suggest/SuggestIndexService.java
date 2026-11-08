package com.dipsource.service.search.suggest;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.service.search.query.TechTermDictionary;
import com.dipsource.service.search.query.TextNormalizer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 자동완성·오타 교정용 단어 사전 인덱스. 기술 용어 사전, 태그, 공개 제목의 단어로 만든다.
 * 검색 로그를 쓰지 않아서 같은 검색어를 반복해 순위를 올릴 수 없다. 단어 빈도는 작성자 수로 세서 한 사람의 도배도 줄인다.
 *
 * <p>Edge N-gram을 고른 이유: Completion Suggester는 접두어 전용 메모리 구조라 빠르지만 자모·초성처럼 필드를 나눠 찾기가 번거롭고,
 * search_as_you_type은 단어 단위 접두어라 "슾" 같은 조합 중 입력을 자모 단위로 다룰 수 없다.
 * 자모 분해 문자열에 Edge N-gram을 걸면 조합 중 글자도 일반 접두어 검색이 된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class SuggestIndexService {

    public enum Source {
        DICT, TAG, WORD
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SuggestTerm(String text, String norm, String source, long doc_count) {
    }

    private static final String TERM_COUNTS_SQL = """
            SELECT term, sum(cnt) AS cnt FROM (
                SELECT s.term, count(DISTINCT p.user_id) AS cnt
                FROM posts p
                CROSS JOIN LATERAL regexp_split_to_table(lower(p.title), '[^a-z0-9가-힣_.#+-]+') AS s(term)
                WHERE p.status = 'ACTIVE' AND p.deleted_at IS NULL
                GROUP BY s.term
                UNION ALL
                SELECT s.term, count(DISTINCT d.user_id) AS cnt
                FROM demos d
                CROSS JOIN LATERAL regexp_split_to_table(lower(d.title), '[^a-z0-9가-힣_.#+-]+') AS s(term)
                WHERE d.status = 'ACTIVE' AND d.deleted_at IS NULL
                GROUP BY s.term
            ) x
            WHERE length(term) BETWEEN 1 AND 50
            GROUP BY term
            """;
    private static final String TAG_COUNTS_SQL = """
            SELECT t.name AS term,
                   (SELECT count(*) FROM post_tags x WHERE x.tag_id = t.id)
                 + (SELECT count(*) FROM demo_tags x WHERE x.tag_id = t.id)
                 + (SELECT count(*) FROM feed_tags x WHERE x.tag_id = t.id) AS cnt
            FROM tags t
            """;

    private final ElasticsearchClient client;
    private final SearchProperties properties;
    private final TechTermDictionary dictionary;
    private final NamedParameterJdbcTemplate jdbc;

    public String alias() {
        return properties.getElasticsearch().getIndexPrefix() + "_suggest";
    }

    /** 사전 전체를 다시 만든다. 공개 범위 변경이 반영되도록 주기적으로 돈다 */
    @Scheduled(fixedDelayString = "${search.suggest.refresh-interval:PT10M}", initialDelayString = "${search.suggest.refresh-initial-delay:PT1M}")
    public void scheduledRebuild() {
        try {
            rebuild();
        } catch (IOException | RuntimeException e) {
            log.warn("검색어 사전 갱신 실패: {}", e.toString());
        }
    }

    /** 사전 인덱스가 없으면 만든다. 서버가 뜰 때 호출한다 */
    public void ensureIndex() throws IOException {
        if (!client.indices().existsAlias(a -> a.name(alias())).value()) {
            rebuild();
        }
    }

    public int rebuild() throws IOException {
        Map<String, SuggestTerm> terms = collectTerms();
        String index = alias() + "_" + System.currentTimeMillis();
        client.indices().create(c -> c.index(index).withJson(new StringReader(MAPPING)));
        BulkRequest.Builder bulk = new BulkRequest.Builder();
        int pending = 0;
        for (Map.Entry<String, SuggestTerm> entry : terms.entrySet()) {
            Map<String, Object> doc = toDocument(entry.getValue());
            bulk.operations(op -> op.index(i -> i.index(index).id(entry.getKey()).document(doc)));
            if (++pending == properties.getElasticsearch().getBulkSize()) {
                client.bulk(bulk.build());
                bulk = new BulkRequest.Builder();
                pending = 0;
            }
        }
        if (pending > 0) {
            client.bulk(bulk.build());
        }
        client.indices().refresh(r -> r.index(index));
        swapAlias(index);
        return terms.size();
    }

    private Map<String, SuggestTerm> collectTerms() {
        Map<String, Long> wordCounts = new HashMap<>();
        jdbc.query(TERM_COUNTS_SQL, rs -> {
            wordCounts.merge(TextNormalizer.normalize(rs.getString("term")), rs.getLong("cnt"), Long::sum);
        });
        Map<String, Long> tagCounts = new HashMap<>();
        Map<String, String> tagNames = new HashMap<>();
        jdbc.query(TAG_COUNTS_SQL, rs -> {
            String norm = TextNormalizer.normalize(rs.getString("term"));
            tagCounts.merge(norm, rs.getLong("cnt"), Long::sum);
            tagNames.putIfAbsent(norm, rs.getString("term"));
        });

        Map<String, SuggestTerm> terms = new HashMap<>();
        wordCounts.forEach((norm, count) -> {
            if (!norm.isBlank()) {
                terms.put(norm, new SuggestTerm(norm, norm, Source.WORD.name(), count));
            }
        });
        tagCounts.forEach((norm, count) -> terms.merge(norm,
                new SuggestTerm(tagNames.get(norm), norm, Source.TAG.name(), count),
                (word, tag) -> new SuggestTerm(tag.text(), norm, Source.TAG.name(), word.doc_count() + tag.doc_count())));
        for (TechTermDictionary.Term term : dictionary.terms()) {
            putDictionaryForm(terms, term.canonical());
            term.aliases().forEach(alias -> putDictionaryForm(terms, alias));
        }
        return terms;
    }

    private static void putDictionaryForm(Map<String, SuggestTerm> terms, String form) {
        String norm = TextNormalizer.normalize(form);
        SuggestTerm existing = terms.get(norm);
        long count = existing == null ? 0 : existing.doc_count();
        String text = existing != null && Source.TAG.name().equals(existing.source()) ? existing.text() : form;
        terms.put(norm, new SuggestTerm(text, norm, Source.DICT.name(), count));
    }

    private static Map<String, Object> toDocument(SuggestTerm term) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("text", term.text());
        doc.put("norm", term.norm());
        doc.put("prefix", term.norm());
        doc.put("jamo", HangulJamo.decompose(term.norm()));
        doc.put("initials", HangulJamo.initials(term.norm()));
        doc.put("source", term.source());
        doc.put("doc_count", term.doc_count());
        return doc;
    }

    /** 접두어로 찾는다. 한글이 있으면 자모로, 초성만 있으면 초성으로 비교한다 */
    public List<SuggestTerm> findByPrefix(String normalizedPrefix, int limit) {
        String field;
        String value;
        if (HangulJamo.isConsonantsOnly(normalizedPrefix)) {
            field = "initials.edge";
            value = normalizedPrefix;
        } else {
            field = "jamo";
            value = HangulJamo.decompose(normalizedPrefix);
        }
        Query query = Query.of(q -> q.match(m -> m.field(field).query(value)));
        return search(query, limit);
    }

    /** 사전 용어 중 접두어로 찾는다(자판 보정 후보) */
    public List<SuggestTerm> findDictionaryByPrefix(String normalizedPrefix, int limit) {
        Query query = Query.of(q -> q.bool(b -> b
                .must(m -> m.match(t -> t.field("prefix").query(normalizedPrefix)))
                .filter(f -> f.term(t -> t.field("source").value(Source.DICT.name())))));
        return search(query, limit);
    }

    public SuggestTerm lookup(String normalized) {
        try {
            GetResponse<SuggestTerm> response = client.get(g -> g.index(alias()).id(normalized), SuggestTerm.class);
            return response.found() ? response.source() : null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** 편집 거리가 가까운 단어. 짧은 단어일수록 허용 거리가 줄어든다(AUTO) */
    public List<SuggestTerm> findFuzzy(String normalized, int limit) {
        Query query = Query.of(q -> q.fuzzy(f -> f.field("norm").value(normalized).fuzziness("AUTO").prefixLength(1)));
        return search(query, limit);
    }

    private List<SuggestTerm> search(Query query, int limit) {
        try {
            SearchResponse<SuggestTerm> response = client.search(s -> s
                    .index(alias()).query(query).size(limit)
                    .sort(o -> o.field(f -> f.field("doc_count").order(SortOrder.Desc)))
                    .sort(o -> o.field(f -> f.field("norm").order(SortOrder.Asc))), SuggestTerm.class);
            return response.hits().hits().stream().map(hit -> hit.source()).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void swapAlias(String index) throws IOException {
        String alias = alias();
        var existing = client.indices().existsAlias(a -> a.name(alias)).value()
                ? client.indices().getAlias(a -> a.name(alias)).result().keySet()
                : java.util.Set.<String>of();
        client.indices().updateAliases(u -> {
            existing.forEach(old -> u.actions(a -> a.remove(r -> r.index(old).alias(alias))));
            return u.actions(a -> a.add(add -> add.index(index).alias(alias)));
        });
        for (String old : existing) {
            client.indices().delete(d -> d.index(old));
        }
    }

    private static final String MAPPING = """
            {
              "settings": {
                "number_of_shards": 1,
                "number_of_replicas": 0,
                "analysis": {
                  "tokenizer": {
                    "edge": {"type": "edge_ngram", "min_gram": 1, "max_gram": 40, "token_chars": []}
                  },
                  "analyzer": {
                    "edge_index": {"type": "custom", "tokenizer": "edge", "filter": ["lowercase"]},
                    "edge_search": {"type": "custom", "tokenizer": "keyword", "filter": ["lowercase"]}
                  }
                }
              },
              "mappings": {
                "dynamic": "strict",
                "properties": {
                  "text": {"type": "keyword", "index": false},
                  "norm": {"type": "keyword"},
                  "prefix": {"type": "text", "analyzer": "edge_index", "search_analyzer": "edge_search"},
                  "jamo": {"type": "text", "analyzer": "edge_index", "search_analyzer": "edge_search"},
                  "initials": {"type": "keyword", "fields": {
                    "edge": {"type": "text", "analyzer": "edge_index", "search_analyzer": "edge_search"}}},
                  "source": {"type": "keyword"},
                  "doc_count": {"type": "long"}
                }
              }
            }
            """;
}
