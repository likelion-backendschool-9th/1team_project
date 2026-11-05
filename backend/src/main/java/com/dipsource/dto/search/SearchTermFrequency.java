package com.dipsource.dto.search;

/** 자동완성 단어 후보. 사전 단어 하나와 그 단어가 나온 횟수. native 쿼리 결과를 받는 인터페이스 projection이다. */
public interface SearchTermFrequency {

    /** 사전 단어(소문자) */
    String getTerm();

    /** 사전 출처(제목·태그)에 이 단어가 나온 횟수 */
    Long getFrequency();
}
