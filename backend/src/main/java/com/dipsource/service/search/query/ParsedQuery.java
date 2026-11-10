package com.dipsource.service.search.query;

import java.util.List;

/**
 * 전처리를 마친 검색어.
 *
 * @param phrase   해시태그를 뺀 일반 검색어 전체(정규화). 해시태그만 검색하면 빈 문자열
 * @param keywords phrase를 공백으로 나눈 단어. 중복 제거
 * @param hashtags 해시태그 비교 키(tagKey). 중복 제거, 개수 제한
 * @param prefix   단어 전체가 아니라 이 글자로 시작하는 단어를 찾는다
 */
public record ParsedQuery(String phrase, List<String> keywords, List<String> hashtags, boolean prefix) {

    public ParsedQuery asPrefix() {
        return new ParsedQuery(phrase, keywords, hashtags, true);
    }

    public boolean hasText() {
        return !keywords.isEmpty();
    }
}
