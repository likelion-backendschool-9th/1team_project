package com.dipsource.service.search.query;

import java.util.List;

/**
 * 전처리를 마친 검색어.
 *
 * @param phrase   해시태그를 뺀 일반 검색어 전체(정규화). 해시태그만 검색하면 빈 문자열
 * @param keywords phrase를 공백으로 나눈 단어. 중복 제거
 * @param hashtags 해시태그 비교 키(tagKey). 중복 제거, 개수 제한
 */
public record ParsedQuery(String phrase, List<String> keywords, List<String> hashtags) {

    public boolean hasText() {
        return !keywords.isEmpty();
    }
}
