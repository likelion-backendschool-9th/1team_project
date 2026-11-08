package com.dipsource.dto.search;

/** 자동완성 후보의 출처. 프론트는 아이콘을 다르게 보여 준다. */
public enum AutocompleteType {
    /** 태그 이름 전체(예: Spring Boot) */
    TAG,
    /** 공개 제목·태그에 나오는 단어 */
    WORD,
    /** 기술 용어 사전의 표기 */
    TERM,
    /** 자판 보정 등 입력과 다른 후보 */
    CORRECTION
}
