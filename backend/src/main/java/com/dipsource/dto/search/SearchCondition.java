package com.dipsource.dto.search;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 정리된 검색 조건. 검색 기술(LIKE, Full-Text 등)과 상관없는 값만 담는다.
 *
 * @param phrase   공백을 한 칸으로 줄인 검색어 전체. 예: "Spring  JPA " → "Spring JPA"
 * @param keywords 공백으로 나눈 단어. 소문자, 중복 제거, 최대 {@link #MAX_KEYWORDS}개
 * @param operator 단어끼리 묶는 방식(and/or)
 * @param sort     정렬
 */
public record SearchCondition(String phrase, List<String> keywords, SearchOperator operator, SearchSort sort) {

    /** 검색에 쓰는 최대 단어 수. 넘는 단어는 버린다 */
    public static final int MAX_KEYWORDS = 5;

    /** @param query 앞뒤 공백을 제거한, 비어 있지 않은 검색어 */
    public static SearchCondition of(String query, SearchOperator operator, SearchSort sort) {
        String[] words = query.split("\\s+");
        List<String> keywords = Arrays.stream(words)
                .map(word -> word.toLowerCase(Locale.ROOT))
                .distinct()
                .limit(MAX_KEYWORDS)
                .toList();
        return new SearchCondition(String.join(" ", words), keywords, operator, sort);
    }
}
