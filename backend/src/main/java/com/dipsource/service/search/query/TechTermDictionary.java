package com.dipsource.service.search.query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 기술 용어 사전(search/tech-terms.json). 동의어, 특수문자 용어 보존, 자판 보정 후보의 기준이다. 한 글자 허용 목록도 함께 읽는다.
 * preserve를 바꾸면 분석기가 바뀌므로 재색인이 필요하다. aliases만 바꾸면 검색 시점에 반영된다.
 */
@Component
public class TechTermDictionary {

    /** @param canonical 대표 표기, aliases 같은 뜻의 다른 표기, preserve 분석 전에 보존할 토큰(없으면 null) */
    public record Term(String canonical, List<String> aliases, String preserve) {
    }

    /**
     * 한 글자 검색 허용 용어(search/single-char-terms.json). 일반 전문 검색 대신 아래 조건으로만 찾는다.
     *
     * @param term       허용하는 글자. 대소문자는 구분하지 않는다
     * @param tags       일치시킬 태그·기술 스택 이름
     * @param titleWords 제목·닉네임에서 독립된 단어로 있을 때 일치로 볼 표기. 영문 한 글자는 비워 둔다("C 레벨" 방지)
     * @param phrases    제목·본문에서 이 구문으로 있을 때 일치로 볼 표현. 예: "c 언어"
     */
    public record SingleCharTerm(String term, List<String> tags, List<String> titleWords, List<String> phrases) {
    }

    private final List<Term> terms;
    /** 정규화한 표기 → 그 표기가 속한 용어 */
    private final Map<String, Term> byForm = new HashMap<>();
    private final Map<String, SingleCharTerm> singleCharacterTerms = new HashMap<>();

    public TechTermDictionary() {
        this.terms = load("search/tech-terms.json", new TypeReference<>() {
        });
        for (Term term : terms) {
            for (String form : forms(term)) {
                byForm.put(TextNormalizer.normalize(form), term);
            }
        }
        List<SingleCharTerm> singles = load("search/single-char-terms.json", new TypeReference<>() {
        });
        singles.forEach(single -> singleCharacterTerms.put(TextNormalizer.normalize(single.term()), single));
    }

    public boolean isSingleCharacterTerm(String word) {
        return singleCharacterTerms.containsKey(word);
    }

    /** 한 글자 허용 용어. 허용 목록에 없으면 null */
    public SingleCharTerm singleCharacterTerm(String word) {
        return singleCharacterTerms.get(word);
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

    private static <T> List<T> load(String resource, TypeReference<List<T>> type) {
        try (InputStream input = new ClassPathResource(resource).getInputStream()) {
            return new ObjectMapper().readValue(input, type);
        } catch (IOException e) {
            throw new UncheckedIOException("기술 용어 사전을 읽지 못했습니다: " + resource, e);
        }
    }
}
