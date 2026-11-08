package com.dipsource.search.eval;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.dipsource.global.auth.UserPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * 고정 평가셋으로 검색 품질을 잰다. 프로필(엔진·랭킹 설정)별 하위 클래스가 같은 평가를 돌리고 결과를 JSON으로 남긴다.
 * 등급은 구현 전에 사람이 정했고, 합성 말뭉치라 절대 수치보다 프로필 간 비교로 읽어야 한다.
 */
@Tag("search-eval")
abstract class AbstractSearchEval {

    private static final int K = 10;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JdbcTemplate jdbc;

    /** 결과 파일 이름 */
    abstract String profile();

    /** 개인화 검색어까지 평가할지. 개인화가 없는 프로필은 해당 검색어를 익명으로 돌린다 */
    boolean personalized() {
        return false;
    }

    String corpusResource() {
        return "search-eval/corpus.json";
    }

    String queriesResource() {
        return "search-eval/queries.json";
    }

    /** 말뭉치를 넣은 뒤 색인 등 엔진별 준비 */
    void prepare(EvalCorpusLoader corpus) throws Exception {
    }

    @Test
    void evaluate() throws Exception {
        EvalCorpusLoader corpus = new EvalCorpusLoader(jdbc);
        corpus.load(corpusResource());
        prepare(corpus);

        JsonNode queries = MAPPER.readTree(new ClassPathResource(queriesResource()).getInputStream());
        List<Map<String, Object>> rows = new ArrayList<>();
        double precision = 0;
        double mrr = 0;
        double ndcg = 0;
        int rankedQueries = 0;
        int suggestQueries = 0;
        int suggestHits = 0;
        List<Long> latencies = new ArrayList<>();
        Map<String, double[]> categories = new LinkedHashMap<>();

        for (JsonNode query : queries) {
            Map<String, Integer> grades = new HashMap<>();
            query.get("relevant").fields().forEachRemaining(e -> grades.put(e.getKey(), e.getValue().asInt()));

            long started = System.nanoTime();
            JsonNode response = search(query, corpus);
            latencies.add((System.nanoTime() - started) / 1_000_000);

            List<String> ranked = new ArrayList<>();
            JsonNode data = response.path("data");
            String type = query.get("type").asText();
            data.path("content").forEach(item -> ranked.add(corpus.keyOf(type, item.get("id").asLong())));

            Map<String, Object> row = new LinkedHashMap<>();
            String category = query.path("category").asText("-");
            double[] stats = categories.computeIfAbsent(category, c -> new double[6]);
            row.put("id", query.get("id").asText());
            row.put("category", category);
            row.put("q", query.get("q").asText());
            row.put("type", type);
            row.put("purpose", query.get("purpose").asText());
            row.put("status", response.path("_status").asInt(200));
            row.put("total", data.path("totalElements").asLong(0));
            row.put("top", ranked.stream().limit(K).toList());
            String suggested = firstNonEmpty(data.path("suggestedQuery").asText(""), data.path("correctedQuery").asText(""));
            row.put("suggestion", suggested);

            if (query.has("suggest")) {
                suggestQueries++;
                boolean hit = query.get("suggest").asText().equalsIgnoreCase(suggested);
                suggestHits += hit ? 1 : 0;
                row.put("suggestHit", hit);
                stats[3]++;
                stats[4] += hit ? 1 : 0;
            }
            if (grades.values().stream().anyMatch(grade -> grade > 0)) {
                double p = EvalMetrics.precisionAt(ranked, grades, K);
                double rr = EvalMetrics.reciprocalRank(ranked, grades);
                double n = EvalMetrics.ndcgAt(ranked, grades, K);
                precision += p;
                mrr += rr;
                ndcg += n;
                rankedQueries++;
                stats[0]++;
                stats[1] += rr;
                stats[2] += n;
                row.put("p10", round(p));
                row.put("rr", round(rr));
                row.put("ndcg10", round(n));
            }
            rows.add(row);
        }

        latencies.sort(Long::compareTo);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("profile", profile());
        summary.put("queries", rows.size());
        summary.put("rankedQueries", rankedQueries);
        summary.put("meanP10", round(precision / rankedQueries));
        summary.put("mrr", round(mrr / rankedQueries));
        summary.put("meanNdcg10", round(ndcg / rankedQueries));
        summary.put("suggestionAccuracy", suggestQueries == 0 ? 0 : round((double) suggestHits / suggestQueries));
        summary.put("latencyMsP50", latencies.get(latencies.size() / 2));
        summary.put("latencyMsMax", latencies.get(latencies.size() - 1));

        Map<String, Object> byCategory = new LinkedHashMap<>();
        categories.forEach((name, stats) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rankedQueries", (int) stats[0]);
            item.put("mrr", stats[0] == 0 ? null : round(stats[1] / stats[0]));
            item.put("meanNdcg10", stats[0] == 0 ? null : round(stats[2] / stats[0]));
            item.put("suggestQueries", (int) stats[3]);
            item.put("suggestionAccuracy", stats[3] == 0 ? null : round(stats[4] / stats[3]));
            byCategory.put(name, item);
        });

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary", summary);
        report.put("byCategory", byCategory);
        report.put("results", rows);
        write(report);
        System.out.println("SEARCH-EVAL " + summary);
    }

    private JsonNode search(JsonNode query, EvalCorpusLoader corpus) throws Exception {
        MockHttpServletRequestBuilder request = get("/api/v1/search")
                .param("q", query.get("q").asText())
                .param("type", query.get("type").asText())
                .param("size", String.valueOf(K))
                .accept(MediaType.APPLICATION_JSON);
        if (query.has("as") && personalized()) {
            UserPrincipal principal = new UserPrincipal(corpus.id(query.get("as").asText()), "USER");
            request.with(authentication(new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities())));
        }
        var result = mockMvc.perform(request).andReturn();
        int status = result.getResponse().getStatus();
        if (status != 200) {
            // 검색 불가(한 글자 등)도 평가 결과로 남긴다
            return MAPPER.createObjectNode().put("_status", status);
        }
        return MAPPER.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private void write(Map<String, Object> report) throws IOException {
        Path dir = Path.of(System.getProperty("search.eval.output", "build/reports/search-eval"));
        Files.createDirectories(dir);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(dir.resolve(profile() + ".json").toFile(), report);
    }

    private static String firstNonEmpty(String a, String b) {
        return a.isEmpty() ? b : a;
    }

    private static double round(double value) {
        return Math.round(value * 10000) / 10000.0;
    }
}
