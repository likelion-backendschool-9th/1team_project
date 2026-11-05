package com.dipsource.dto.search;

/**
 * 자동완성 후보 하나.
 *
 * @param text 검색창에 채울 검색어. 여러 단어를 입력했으면 앞 단어를 그대로 두고 마지막 단어만 완성한다
 * @param type 후보 출처
 */
public record AutocompleteResponse(String text, AutocompleteType type) {
}
