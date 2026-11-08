package com.dipsource.service.search.suggest;

import com.dipsource.dto.search.AutocompleteResponse;
import com.dipsource.dto.search.AutocompleteType;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.search.AutocompleteService;
import com.dipsource.service.search.query.TextNormalizer;
import com.dipsource.service.search.suggest.SuggestIndexService.SuggestTerm;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Elasticsearch 자동완성. 순서: 입력과 같음 → 접두어 일치 → 사전 용어 → 자판 보정.
 * 일반 오타 교정은 입력 중에는 하지 않는다. 쓰다 만 단어는 대부분 오타로 보이기 때문이다.
 */
@Service("engineAutocompleteService")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class EngineAutocompleteService implements AutocompleteService {

    static final int MAX_SUGGESTIONS = 8;
    static final int CANDIDATES = 30;
    static final int VERIFIED_CANDIDATES = 16;
    static final int MAX_INPUT_LENGTH = 50;

    private final SuggestIndexService suggestIndex;

    @Override
    public List<AutocompleteResponse> autocomplete(String query) {
        String input = TextNormalizer.normalize(query);
        if (input.isEmpty()) {
            return List.of();
        }
        if (TextNormalizer.codePointLength(input) > MAX_INPUT_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "검색어는 " + MAX_INPUT_LENGTH + "자 이하로 입력해 주세요");
        }
        int lastSpace = input.lastIndexOf(' ');
        String leading = input.substring(0, lastSpace + 1);
        String lastWord = input.substring(lastSpace + 1);

        Map<String, AutocompleteResponse> suggestions = new LinkedHashMap<>();
        // 제목에서 뽑은 후보(표시 문자열 → 사전 단어). 사전이 만들어진 뒤 글이 지워졌을 수 있어서 응답 전에 확인한다
        Map<String, String> titleWords = new HashMap<>();
        // 입력 전체로 시작하는 용어·태그(스프링 부트, Spring Boot)
        suggestIndex.findByPrefix(input, CANDIDATES).stream()
                .filter(term -> !SuggestIndexService.Source.WORD.name().equals(term.source()))
                .sorted(order(input))
                .forEach(term -> put(suggestions, term.text(), typeOf(term)));
        // 앞 단어는 그대로 두고 마지막 단어만 완성한다
        if (!lastWord.isEmpty()) {
            suggestIndex.findByPrefix(lastWord, CANDIDATES).stream()
                    .sorted(order(lastWord))
                    .forEach(term -> {
                        put(suggestions, leading + term.norm(), AutocompleteType.WORD);
                        if (SuggestIndexService.Source.WORD.name().equals(term.source())) {
                            titleWords.put((leading + term.norm()), term.norm());
                        }
                    });
        }
        if (suggestions.size() < MAX_SUGGESTIONS) {
            addKeyboardCandidates(suggestions, leading, lastWord);
        }
        return withoutStaleTitleWords(suggestions, titleWords);
    }

    /** 앞쪽 후보부터 살아 있는 제목 단어만 남긴다. 빠진 자리를 채울 수 있도록 응답 수보다 넉넉히 확인한다 */
    private List<AutocompleteResponse> withoutStaleTitleWords(Map<String, AutocompleteResponse> suggestions, Map<String, String> titleWords) {
        List<AutocompleteResponse> ordered = suggestions.values().stream().limit(VERIFIED_CANDIDATES).toList();
        Set<String> toCheck = ordered.stream().map(item -> titleWords.get(item.text()))
                .filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        Set<String> live = suggestIndex.findLiveTitleWords(toCheck);
        return ordered.stream()
                .filter(item -> !titleWords.containsKey(item.text()) || live.contains(titleWords.get(item.text())))
                .limit(MAX_SUGGESTIONS)
                .toList();
    }

    private void addKeyboardCandidates(Map<String, AutocompleteResponse> suggestions, String leading, String lastWord) {
        String latin = KeyboardLayoutConverter.toLatin(lastWord);
        String converted = latin != null ? latin : KeyboardLayoutConverter.toHangul(lastWord);
        if (converted == null || converted.length() < SearchSuggester.MIN_KEYBOARD_LENGTH) {
            return;
        }
        suggestIndex.findDictionaryByPrefix(converted, CANDIDATES).stream()
                .sorted(order(converted))
                .forEach(term -> put(suggestions, leading + term.norm(), AutocompleteType.CORRECTION));
    }

    private static void put(Map<String, AutocompleteResponse> suggestions, String text, AutocompleteType type) {
        suggestions.putIfAbsent(text.toLowerCase(java.util.Locale.ROOT), new AutocompleteResponse(text, type));
    }

    private static AutocompleteType typeOf(SuggestTerm term) {
        return SuggestIndexService.Source.TAG.name().equals(term.source()) ? AutocompleteType.TAG : AutocompleteType.TERM;
    }

    /** 입력과 같은 후보 → 실제 글자가 접두어인 후보 → 사전 용어 → 많이 쓰인 순 → 짧은 순 */
    private static Comparator<SuggestTerm> order(String input) {
        return Comparator
                .comparing((SuggestTerm term) -> !term.norm().equals(input))
                .thenComparing(term -> !term.norm().startsWith(input))
                .thenComparing(term -> !SuggestIndexService.Source.DICT.name().equals(term.source()))
                .thenComparing(SuggestTerm::doc_count, Comparator.reverseOrder())
                .thenComparing(term -> term.norm().length())
                .thenComparing(SuggestTerm::norm);
    }
}
