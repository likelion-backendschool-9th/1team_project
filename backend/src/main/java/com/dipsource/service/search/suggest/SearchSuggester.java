package com.dipsource.service.search.suggest;

import com.dipsource.dto.search.SearchSuggestion;
import com.dipsource.dto.search.SuggestionType;
import com.dipsource.service.search.query.ParsedQuery;
import com.dipsource.service.search.query.TechTermDictionary;
import com.dipsource.service.search.query.TextNormalizer;
import com.dipsource.service.search.suggest.SuggestIndexService.SuggestTerm;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 검색어 제안. 자판 보정과 오타 교정은 별개의 기능이고 자판 보정이 먼저다.
 * 원래 검색어는 절대 바꾸지 않고 제안만 만든다. 결과가 있어도 제안할 수 있다(글에 같은 오타가 있을 수 있으므로).
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class SearchSuggester {

    /** 오타 후보의 빈도가 입력 단어의 이 배수 이상이어야 제안한다 */
    static final int TYPO_FREQUENCY_RATIO = 3;
    static final int MIN_TYPO_LENGTH = 3;
    static final int FUZZY_CANDIDATES = 10;
    /** "ㄱ" → r처럼 짧은 변환은 우연히 사전과 맞을 가능성이 크다 */
    static final int MIN_KEYBOARD_LENGTH = 2;
    static final int MIN_SYLLABLE_KEYBOARD_LENGTH = 4;
    private static final Set<String> INFLECTION_ENDINGS = Set.of("s", "es", "ed", "d", "ing", "er", "ers", "ly");

    private final TechTermDictionary dictionary;
    private final SuggestIndexService suggestIndex;

    public Optional<SearchSuggestion> suggest(ParsedQuery query) {
        if (!query.hasText()) {
            return Optional.empty();
        }
        return rewrite(query, this::correctKeyboard, SuggestionType.KEYBOARD)
                .or(() -> rewrite(query, this::correctTypo, SuggestionType.TYPO));
    }

    /**
     * 입력으로 시작하는 단어를 제안한다. 자모 단위로 비교하므로 입력 중인 글자도 찾는다(슾 → 스프링, 잡 → 자바).
     * 사전 용어를 먼저 고르고, 그중에서는 짧은 표기를 고른다(스프링부트보다 스프링). 사전에 없는 단어는 많이 쓰인 순이다.
     */
    public Optional<SearchSuggestion> suggestCompletion(ParsedQuery query) {
        String word = query.keywords().get(0);
        return suggestIndex.findByPrefix(word, FUZZY_CANDIDATES * 2).stream()
                .filter(term -> !term.norm().equals(word) && (isDictionaryTerm(term) || term.doc_count() > 0))
                .min(Comparator
                        .comparing((SuggestTerm term) -> !isDictionaryTerm(term))
                        .thenComparing(term -> isDictionaryTerm(term) ? term.norm().length() : 0)
                        .thenComparing(SuggestTerm::doc_count, Comparator.reverseOrder())
                        .thenComparing(term -> term.norm().length())
                        .thenComparing(SuggestTerm::norm))
                .map(term -> new SearchSuggestion(term.norm(), SuggestionType.COMPLETION));
    }

    private static boolean isDictionaryTerm(SuggestTerm term) {
        return SuggestIndexService.Source.DICT.name().equals(term.source());
    }

    private static Optional<SearchSuggestion> rewrite(ParsedQuery query, Function<String, String> correct, SuggestionType type) {
        List<String> corrected = new ArrayList<>();
        boolean changed = false;
        for (String keyword : query.keywords()) {
            String word = correct.apply(keyword);
            changed |= !word.equals(keyword);
            if (!corrected.contains(word)) {
                corrected.add(word);
            }
        }
        return changed ? Optional.of(new SearchSuggestion(String.join(" ", corrected), type)) : Optional.empty();
    }

    /** 사전에 있는 영문·한글 표기로 바뀔 때만 후보로 삼는다 */
    private String correctKeyboard(String word) {
        if (dictionary.contains(word)) {
            return word;
        }
        String candidate = keyboardCandidate(word);
        if (candidate == null) {
            return word;
        }
        SuggestTerm own = suggestIndex.lookup(word);
        SuggestTerm target = suggestIndex.lookup(candidate);
        long ownCount = own == null ? 0 : own.doc_count();
        long targetCount = target == null ? 0 : target.doc_count();
        // 입력 그대로의 단어가 실제 글에서 더 많이 쓰이면 정상 단어로 본다
        return ownCount > targetCount ? word : candidate;
    }

    /** 완성된 음절로만 된 입력은 정상 한국어일 가능성이 커서 더 긴 변환만 믿는다("해" → go 방지) */
    private static int minLatinLength(String word) {
        boolean syllablesOnly = word.codePoints().allMatch(HangulJamo::isSyllable);
        return syllablesOnly ? MIN_SYLLABLE_KEYBOARD_LENGTH : MIN_KEYBOARD_LENGTH;
    }

    private String keyboardCandidate(String word) {
        String latin = KeyboardLayoutConverter.toLatin(word);
        if (latin != null && latin.length() >= minLatinLength(word) && dictionary.contains(latin)) {
            return latin;
        }
        String hangul = KeyboardLayoutConverter.toHangul(word);
        if (hangul != null && TextNormalizer.codePointLength(hangul) >= MIN_KEYBOARD_LENGTH && dictionary.contains(hangul)) {
            return hangul;
        }
        return null;
    }

    /**
     * 오타가 아니라 같은 단어의 다른 형태인지. 입력이 후보에 포함되면(jav → java, token → tokens) 이미 그 말을 쓴 것으로 보고,
     * 후보가 입력의 앞부분이면 영어 어미(tokens → token)나 한국어 조사가 붙은 경우만 해당한다. reactt → react는 오타로 본다.
     */
    private static boolean isVariantOf(String word, String candidate) {
        if (candidate.contains(word)) {
            return true;
        }
        if (!word.startsWith(candidate)) {
            return word.contains(candidate);
        }
        String ending = word.substring(candidate.length());
        return INFLECTION_ENDINGS.contains(ending) || ending.codePoints().allMatch(HangulJamo::isSyllable);
    }

    /** 훨씬 자주 쓰이는 단어거나, 쓰임이 비슷해도 사전에 있는 기술 용어면 더 나은 철자로 본다 */
    private boolean isBetterSpelling(SuggestTerm candidate, long ownCount) {
        if (dictionary.contains(candidate.norm())) {
            return candidate.doc_count() >= Math.max(1, ownCount);
        }
        return candidate.doc_count() >= Math.max(1, ownCount * TYPO_FREQUENCY_RATIO);
    }

    private String correctTypo(String word) {
        if (TextNormalizer.codePointLength(word) < MIN_TYPO_LENGTH || dictionary.contains(word)) {
            return word;
        }
        SuggestTerm own = suggestIndex.lookup(word);
        long ownCount = own == null ? 0 : own.doc_count();
        return suggestIndex.findFuzzy(word, FUZZY_CANDIDATES).stream()
                .filter(candidate -> !candidate.norm().equals(word))
                .filter(candidate -> !isVariantOf(word, candidate.norm()))
                .filter(candidate -> isBetterSpelling(candidate, ownCount))
                .findFirst()
                .map(SuggestTerm::norm)
                .orElse(word);
    }
}
