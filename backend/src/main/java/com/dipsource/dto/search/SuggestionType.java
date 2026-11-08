package com.dipsource.dto.search;

/** 제안 검색어를 만든 방법. 프론트가 문구를 고르는 데 쓴다. */
public enum SuggestionType {
    /** 한/영 자판을 잘못 두고 입력한 경우 */
    KEYBOARD,
    /** 일반 오타 */
    TYPO
}
