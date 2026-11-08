package com.dipsource.service.search.index;

import com.dipsource.dto.search.SearchType;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.service.search.query.TechTermDictionary;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 인덱스 설정과 매핑. 분석기는 네 인덱스가 같이 쓰고, 문서 언어를 알 수 없어서 언어별 서브필드에 모두 색인한다.
 * 원문을 그대로 받는 기본 필드와 언어별 분석 필드를 나눠서, 정확한 표기가 형태소 분석 결과보다 먼저 맞도록 한다.
 */
@Component
@RequiredArgsConstructor
public class SearchIndexDefinitions {

    /** 부정 표현(안, 못)이 사라지지 않도록 Nori 기본 불용 품사에서 부사(MAG)를 뺐다 */
    private static final List<String> NORI_STOP_TAGS = List.of(
            "E", "IC", "J", "MAJ", "MM", "SP", "SSC", "SSO", "SC", "SE",
            "XPN", "XSA", "XSN", "XSV", "UNA", "NA", "VSV");

    private final SearchProperties properties;
    private final TechTermDictionary dictionary;

    /** 인덱스 생성 요청 본문 */
    public Map<String, Object> indexBody(SearchType type) {
        return Map.of("settings", settings(), "mappings", mappings(type));
    }

    private Map<String, Object> settings() {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("number_of_shards", properties.getElasticsearch().getShards());
        settings.put("number_of_replicas", properties.getElasticsearch().getReplicas());
        settings.put("max_ngram_diff", 1);
        settings.put("analysis", analysis());
        return settings;
    }

    private Map<String, Object> analysis() {
        boolean tech = properties.getAnalysis().isTechTerms();
        List<String> charFilters = tech ? List.of("icu_normalizer", "tech_terms") : List.of("icu_normalizer");

        Map<String, Object> charFilter = new LinkedHashMap<>();
        if (tech) {
            charFilter.put("tech_terms", Map.of("type", "mapping", "mappings", techMappings()));
        }

        Map<String, Object> tokenizer = new LinkedHashMap<>();
        tokenizer.put("nori_mixed", Map.of(
                "type", "nori_tokenizer",
                "decompound_mode", "mixed",
                "discard_punctuation", true,
                "user_dictionary_rules", noriUserRules()));
        tokenizer.put("nickname_ngram", Map.of(
                "type", "ngram", "min_gram", 2, "max_gram", 3, "token_chars", List.of("letter", "digit")));

        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("ko_pos", Map.of("type", "nori_part_of_speech", "stoptags", NORI_STOP_TAGS));
        filter.put("en_stem", Map.of("type", "stemmer", "language", "english"));

        Map<String, Object> analyzer = new LinkedHashMap<>();
        analyzer.put("dip_default", custom(charFilters, "icu_tokenizer", List.of("lowercase")));
        analyzer.put("dip_ko", custom(charFilters, "nori_mixed", List.of("ko_pos", "nori_readingform", "lowercase")));
        analyzer.put("dip_en", custom(charFilters, "standard", List.of("lowercase", "en_stem")));
        analyzer.put("dip_ja", custom(charFilters, "kuromoji_tokenizer",
                List.of("kuromoji_baseform", "kuromoji_part_of_speech", "cjk_width", "ja_stop", "kuromoji_stemmer", "lowercase")));
        analyzer.put("dip_zh", custom(charFilters, "smartcn_tokenizer", List.of("lowercase")));
        analyzer.put("dip_ngram", custom(List.of("icu_normalizer"), "nickname_ngram", List.of("lowercase")));

        Map<String, Object> analysis = new LinkedHashMap<>();
        analysis.put("char_filter", charFilter);
        analysis.put("tokenizer", tokenizer);
        analysis.put("filter", filter);
        analysis.put("analyzer", analyzer);
        return analysis;
    }

    private static Map<String, Object> custom(List<String> charFilters, String tokenizer, List<String> filters) {
        return Map.of("type", "custom", "char_filter", charFilters, "tokenizer", tokenizer, "filter", filters);
    }

    /** "c++ => cplusplus". icu_normalizer가 먼저 소문자로 바꾸므로 소문자만 등록한다 */
    private List<String> techMappings() {
        return dictionary.terms().stream()
                .filter(term -> term.preserve() != null)
                .map(term -> term.canonical() + " => " + term.preserve())
                .toList();
    }

    /** 사전에 있는 띄어쓰기 없는 한글 표기를 한 단어로 인식시킨다 */
    private List<String> noriUserRules() {
        List<String> rules = new ArrayList<>();
        for (TechTermDictionary.Term term : dictionary.terms()) {
            term.aliases().stream()
                    .filter(alias -> alias.matches("[가-힣]{2,}"))
                    .forEach(rules::add);
        }
        return rules.stream().distinct().toList();
    }

    private Map<String, Object> mappings(SearchType type) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("doc_id", Map.of("type", "long"));
        fields.put("created_at", Map.of("type", "date"));
        fields.put("quality", Map.of("type", "float"));
        switch (type) {
            case POST -> {
                fields.put("author_id", Map.of("type", "long"));
                fields.put("title", text());
                fields.put("title_norm", Map.of("type", "keyword"));
                fields.put("content", text());
                putTags(fields);
            }
            case DEMO -> {
                fields.put("author_id", Map.of("type", "long"));
                fields.put("title", text());
                fields.put("title_norm", Map.of("type", "keyword"));
                fields.put("description", text());
                putTags(fields);
            }
            case FEED -> {
                fields.put("author_id", Map.of("type", "long"));
                fields.put("content", text());
                fields.put("demo_text", text());
                putTags(fields);
            }
            case USER -> {
                Map<String, Object> nickname = new LinkedHashMap<>(text());
                @SuppressWarnings("unchecked")
                Map<String, Object> subFields = new LinkedHashMap<>((Map<String, Object>) nickname.get("fields"));
                subFields.put("ngram", Map.of("type", "text", "analyzer", "dip_ngram"));
                nickname.put("fields", subFields);
                fields.put("nickname", nickname);
                fields.put("nickname_norm", Map.of("type", "keyword"));
                fields.put("bio", text());
                fields.put("tech_stacks", Map.of("type", "keyword"));
                fields.put("tech_text", text());
                fields.put("github_username", text());
            }
            case ALL -> throw new IllegalArgumentException("ALL은 인덱스가 아니다");
        }
        return Map.of("dynamic", "strict", "properties", fields);
    }

    private static void putTags(Map<String, Object> fields) {
        fields.put("tags", Map.of("type", "keyword"));
        fields.put("tags_text", text());
    }

    private static Map<String, Object> text() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("type", "text");
        field.put("analyzer", "dip_default");
        field.put("fields", Map.of(
                "ko", Map.of("type", "text", "analyzer", "dip_ko"),
                "en", Map.of("type", "text", "analyzer", "dip_en"),
                "ja", Map.of("type", "text", "analyzer", "dip_ja"),
                "zh", Map.of("type", "text", "analyzer", "dip_zh")));
        return field;
    }
}
