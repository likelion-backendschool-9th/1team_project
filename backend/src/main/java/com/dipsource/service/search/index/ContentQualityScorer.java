package com.dipsource.service.search.index;

import com.dipsource.global.config.SearchProperties;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 색인 시점의 콘텐츠 품질 계수(1이면 감점 없음). 검색 순위를 노린 단어 반복만 깎는다.
 * 단어를 많이 쓰는 정상 기술 문서가 다치지 않도록 신호 하나로는 감점하지 않고, 두 개 이상 겹칠 때만 감점한다.
 * 문서를 지우지는 않으므로 잘못 판단해도 순위만 내려간다.
 */
@Component
@RequiredArgsConstructor
public class ContentQualityScorer {

    /** 이보다 짧은 글은 비율이 흔들려서 판단하지 않는다 */
    static final int MIN_TOKENS = 30;
    static final double TOP_TOKEN_RATIO = 0.35;
    static final double UNIQUE_RATIO = 0.15;
    static final int ADJACENT_RUN = 10;
    static final double REPEATED_SENTENCE_RATIO = 0.5;
    static final int MIN_SENTENCES = 5;
    static final double REPEATED_PHRASE_RATIO = 0.6;
    static final int REQUIRED_SIGNALS = 2;

    private final SearchProperties properties;

    public float score(String text) {
        if (text == null || text.isBlank()) {
            return 1.0f;
        }
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[\\s\\p{Punct}]+");
        if (tokens.length < MIN_TOKENS) {
            return 1.0f;
        }
        int signals = 0;
        Map<String, Integer> frequencies = new HashMap<>();
        Arrays.stream(tokens).forEach(token -> frequencies.merge(token, 1, Integer::sum));
        if ((double) Collections.max(frequencies.values()) / tokens.length >= TOP_TOKEN_RATIO) {
            signals++;
        }
        if ((double) frequencies.size() / tokens.length <= UNIQUE_RATIO) {
            signals++;
        }
        if (longestRun(tokens) >= ADJACENT_RUN) {
            signals++;
        }
        if (repeatedSentenceRatio(text) >= REPEATED_SENTENCE_RATIO) {
            signals++;
        }
        if (repeatedPhraseRatio(tokens) >= REPEATED_PHRASE_RATIO) {
            signals++;
        }
        if (signals < REQUIRED_SIGNALS) {
            return 1.0f;
        }
        return 1.0f - properties.getRanking().getMaxSpamPenalty();
    }

    private static int longestRun(String[] tokens) {
        int longest = 1;
        int run = 1;
        for (int i = 1; i < tokens.length; i++) {
            run = tokens[i].equals(tokens[i - 1]) ? run + 1 : 1;
            longest = Math.max(longest, run);
        }
        return longest;
    }

    /** 세 단어 묶음 중 앞에서 이미 나온 묶음의 비율. 마침표 없이 같은 문구를 반복해도 잡힌다 */
    private static double repeatedPhraseRatio(String[] tokens) {
        Set<String> seen = new HashSet<>();
        int repeated = 0;
        for (int i = 0; i + 2 < tokens.length; i++) {
            if (!seen.add(tokens[i] + " " + tokens[i + 1] + " " + tokens[i + 2])) {
                repeated++;
            }
        }
        return (double) repeated / (tokens.length - 2);
    }

    private static double repeatedSentenceRatio(String text) {
        List<String> sentences = Arrays.stream(text.split("[.!?\\n]+"))
                .map(String::strip).filter(sentence -> !sentence.isEmpty()).toList();
        if (sentences.size() < MIN_SENTENCES) {
            return 0;
        }
        long distinct = sentences.stream().distinct().count();
        return 1 - (double) distinct / sentences.size();
    }
}
