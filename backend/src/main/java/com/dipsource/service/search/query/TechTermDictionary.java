package com.dipsource.service.search.query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 기술 용어 사전(search/tech-terms.json). 동의어, 특수문자 용어 보존, 한 글자 용어, 자판 보정 후보의 기준이다.
 * preserve를 바꾸면 분석기가 바뀌므로 재색인이 필요하다. aliases만 바꾸면 검색 시점에 반영된다.
 */
@Component
public class TechTermDictionary {

    /** @param canonical 대표 표기, aliases 같은 뜻의 다른 표기, preserve 분석 전에 보존할 토큰(없으면 null) */
    public record Term(String canonical, List<String> aliases, String preserve) {
    }

    private final List<Term> terms;
    /** 정규화한 표기 → 그 표기가 속한 용어 */
    private final Map<String, Term> byForm = new HashMap<>();
    private final Set<String> singleCharacterTerms;

    public TechTermDictionary() {
        this.terms = load();
        Set<String> singles = new LinkedHashSet<>();
        for (Term term : terms) {
            for (String form : forms(term)) {
                String normalized = TextNormalizer.normalize(form);
                byForm.put(normalized, term);
                if (TextNormalizer.codePointLength(normalized) == 1) {
                    singles.add(normalized);
                }
            }
        }
        this.singleCharacterTerms = Set.copyOf(singles);
    }

    public boolean isSingleCharacterTerm(String word) {
        return singleCharacterTerms.contains(word);
    }

    public boolean contains(String normalizedForm) {
        return byForm.containsKey(normalizedForm);
    }

    public List<Term> terms() {
        return terms;
    }

    /** 대표 표기. 사전에 없으면 null */
    public String canonicalOf(String normalizedForm) {
        Term term = byForm.get(normalizedForm);
        return term == null ? null : term.canonical();
    }

    /** 표기가 속한 용어의 다른 표기들(자기 자신 제외, 정규화). 사전에 없으면 빈 목록 */
    public List<String> synonymsOf(String normalizedForm) {
        Term term = byForm.get(normalizedForm);
        if (term == null) {
            return List.of();
        }
        List<String> synonyms = new ArrayList<>();
        for (String form : forms(term)) {
            String normalized = TextNormalizer.normalize(form);
            if (!normalized.equals(normalizedForm) && !synonyms.contains(normalized)) {
                synonyms.add(normalized);
            }
        }
        return synonyms;
    }

    private static List<String> forms(Term term) {
        List<String> forms = new ArrayList<>();
        forms.add(term.canonical());
        forms.addAll(term.aliases());
        return forms;
    }

    private static List<Term> load() {
        try (InputStream input = new ClassPathResource("search/tech-terms.json").getInputStream()) {
            return new ObjectMapper().readValue(input, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new UncheckedIOException("기술 용어 사전을 읽지 못했습니다", e);
        }
    }
}
