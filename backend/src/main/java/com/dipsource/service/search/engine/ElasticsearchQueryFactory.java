package com.dipsource.service.search.engine;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchType;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.global.config.SearchProperties.TypePolicy;
import com.dipsource.service.search.query.ParsedQuery;
import com.dipsource.service.search.query.TechTermDictionary;
import com.dipsource.service.search.query.TextNormalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 검색어를 Elasticsearch 쿼리로 바꾼다. 단어 하나마다 필드별 BM25를 묶고(dis_max), 단어끼리 and/or를 적용한다.
 * 필드를 통째로 묶지 않고 단어별로 묶기 때문에 제목에 spring, 본문에 jpa가 있어도 and로 찾힌다.
 */
@Component
@RequiredArgsConstructor
public class ElasticsearchQueryFactory {

    /** 정확 일치 쿼리 이름. 응답의 matched_queries로 정확 일치 여부를 받는다 */
    static final String EXACT_QUERY_NAME = "exact";

    /** 검색 의도만 나타내는 단어. 문서에 없어도 되지만 있으면 점수를 받는다. 부정 표현은 넣지 않는다 */
    private static final List<String> OPTIONAL_WORDS = List.of(
            "방법", "방법은", "어떻게", "하는법", "how", "to", "the", "a", "an", "of", "in", "for", "with");

    private final SearchProperties properties;
    private final TechTermDictionary dictionary;

    public Query build(SearchPort.Request request) {
        boolean flat = properties.getRanking().isFlat();
        TypePolicy policy = properties.getRanking().policy(request.type().name().toLowerCase(Locale.ROOT));
        ParsedQuery query = request.query();

        BoolQuery.Builder bool = new BoolQuery.Builder();
        applyHashtags(bool, policy, query, request.operator());
        applyExclusions(bool, request);

        if (query.hasText()) {
            applyKeywords(bool, policy, query, request.operator(), flat);
            if (!flat) {
                applyPhraseAndExact(bool, policy, query);
            }
        } else {
            bool.must(m -> m.matchAll(a -> a));
        }
        Query scored = Query.of(q -> q.bool(bool.build()));
        return wrapQuality(scored, flat);
    }

    private Query wrapQuality(Query scored, boolean flat) {
        if (flat) {
            return scored;
        }
        return Query.of(q -> q.functionScore(fs -> fs
                .query(scored)
                .functions(f -> f.fieldValueFactor(v -> v.field("quality").missing(1.0)))
                .boostMode(co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode.Multiply)));
    }

    /** 명시적 해시태그는 필수 조건이다. 여러 개면 op=or일 때만 하나라도 일치하면 되고, 기본은 모두 일치다 */
    private void applyHashtags(BoolQuery.Builder bool, TypePolicy policy, ParsedQuery query, SearchOperator operator) {
        if (query.hashtags().isEmpty()) {
            return;
        }
        if (operator == SearchOperator.OR) {
            List<FieldValue> values = query.hashtags().stream().map(FieldValue::of).toList();
            bool.filter(f -> f.terms(t -> t.field(policy.getKeywordField()).terms(v -> v.value(values))));
            return;
        }
        for (String tag : query.hashtags()) {
            bool.filter(f -> f.term(t -> t.field(policy.getKeywordField()).value(tag)));
        }
    }

    private void applyExclusions(BoolQuery.Builder bool, SearchPort.Request request) {
        if (!request.excludedIds().isEmpty()) {
            bool.mustNot(m -> m.ids(i -> i.values(request.excludedIds().stream().map(String::valueOf).toList())));
        }
        if (!request.excludedAuthors().isEmpty()) {
            if (request.type() == SearchType.USER) {
                bool.mustNot(m -> m.ids(i -> i.values(request.excludedAuthors().stream().map(String::valueOf).toList())));
            } else {
                List<FieldValue> authors = request.excludedAuthors().stream().map(FieldValue::of).toList();
                bool.mustNot(m -> m.terms(t -> t.field("author_id").terms(v -> v.value(authors))));
            }
        }
    }

    private void applyKeywords(BoolQuery.Builder bool, TypePolicy policy, ParsedQuery query, SearchOperator operator,
                               boolean flat) {
        List<String> required = new ArrayList<>();
        List<String> optional = new ArrayList<>();
        for (String keyword : query.keywords()) {
            boolean droppable = operator == SearchOperator.AUTO && OPTIONAL_WORDS.contains(keyword)
                    && query.keywords().size() > 1;
            (droppable ? optional : required).add(keyword);
        }
        if (required.isEmpty()) {
            required.addAll(optional);
            optional.clear();
        }
        List<Query> groups = required.stream().map(keyword -> keywordGroup(policy, keyword, flat)).toList();
        boolean natural = operator == SearchOperator.AUTO
                && required.size() >= properties.getRanking().getNaturalMinKeywords();
        if (operator == SearchOperator.OR || natural) {
            groups.forEach(bool::should);
            bool.minimumShouldMatch(natural ? properties.getRanking().getNaturalMinimumShouldMatch() : "1");
        } else {
            groups.forEach(bool::must);
        }
        optional.forEach(keyword -> bool.should(keywordGroup(policy, keyword, flat)));
    }

    /** 단어 하나를 찾는 필드별 쿼리 묶음. 가장 잘 맞는 필드의 점수를 쓰고 나머지는 tie_breaker만큼만 더한다 */
    private Query keywordGroup(TypePolicy policy, String keyword, boolean flat) {
        List<Query> clauses = new ArrayList<>();
        for (Map.Entry<String, Float> field : policy.getFields().entrySet()) {
            float boost = flat ? 1f : field.getValue();
            clauses.add(fieldMatch(field.getKey(), keyword, boost));
        }
        clauses.add(keywordField(policy, keyword, flat ? 1f : policy.getKeywordBoost()));
        addSynonyms(clauses, policy, keyword, flat);
        return Query.of(q -> q.disMax(d -> d.queries(clauses).tieBreaker((double) properties.getRanking().getTieBreaker())));
    }

    private void addSynonyms(List<Query> clauses, TypePolicy policy, String keyword, boolean flat) {
        if (flat || !properties.getAnalysis().isSynonyms()) {
            return;
        }
        float weight = properties.getRanking().getSynonymBoost();
        for (String synonym : dictionary.synonymsOf(keyword)) {
            for (Map.Entry<String, Float> field : policy.getFields().entrySet()) {
                clauses.add(fieldMatch(field.getKey(), synonym, field.getValue() * weight));
            }
            clauses.add(keywordField(policy, synonym, policy.getKeywordBoost() * weight));
        }
    }

    private Query keywordField(TypePolicy policy, String word, float boost) {
        String key = TextNormalizer.tagKey(word);
        return Query.of(q -> q.term(t -> t.field(policy.getKeywordField()).value(key).boost(boost)));
    }

    /** 원문 필드와, 단어에 쓰인 문자에 맞는 언어 서브필드에서 찾는다 */
    private Query fieldMatch(String field, String word, float boost) {
        List<Query> languages = new ArrayList<>();
        languages.add(match(field, word));
        if (!field.endsWith(".ngram") && properties.getAnalysis().isLanguageFields()) {
            for (String language : languagesOf(word)) {
                languages.add(match(field + "." + language, word));
            }
        }
        if (languages.size() == 1) {
            return Query.of(q -> q.match(m -> m.field(field).query(word).operator(Operator.And).boost(boost)));
        }
        return Query.of(q -> q.disMax(d -> d.queries(languages).boost(boost)));
    }

    private static Query match(String field, String word) {
        return Query.of(q -> q.match(m -> m.field(field).query(word).operator(Operator.And)));
    }

    /** 일본어·중국어는 한자를 같이 쓰므로 가나가 없는 한자 단어는 두 분석기를 모두 본다 */
    static List<String> languagesOf(String word) {
        boolean hangul = false;
        boolean latin = false;
        boolean kana = false;
        boolean han = false;
        for (int codePoint : word.codePoints().toArray()) {
            Character.UnicodeScript script = Character.UnicodeScript.of(codePoint);
            hangul |= script == Character.UnicodeScript.HANGUL;
            latin |= script == Character.UnicodeScript.LATIN;
            kana |= script == Character.UnicodeScript.HIRAGANA || script == Character.UnicodeScript.KATAKANA;
            han |= script == Character.UnicodeScript.HAN;
        }
        List<String> languages = new ArrayList<>();
        if (hangul) {
            languages.add("ko");
        }
        if (latin) {
            languages.add("en");
        }
        if (kana) {
            languages.add("ja");
        } else if (han) {
            languages.add("zh");
            languages.add("ja");
        }
        return languages;
    }

    /** 제목·닉네임 전체 일치와 구문 일치. 후보 안에서 위로 올리는 가산점이고, 일치 조건에는 영향을 주지 않는다 */
    private void applyPhraseAndExact(BoolQuery.Builder bool, TypePolicy policy, ParsedQuery query) {
        if (policy.getExactField() != null) {
            String exact = query.phrase();
            bool.should(s -> s.constantScore(c -> c
                    .queryName(EXACT_QUERY_NAME)
                    .filter(f -> f.term(t -> t.field(policy.getExactField()).value(exact)))
                    .boost(policy.getExactBoost())));
        }
        if (query.keywords().size() > 1) {
            String field = policy.getPhraseField();
            bool.should(s -> s.matchPhrase(p -> p.field(field).query(query.phrase()).boost(policy.getPhraseBoost())));
        }
    }
}
