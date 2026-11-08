package com.dipsource.dto.search;

/** 제안 검색어. 원래 검색어를 바꾸지 않고 "이것을 찾으셨나요?"로만 보여 준다. */
public record SearchSuggestion(String query, SuggestionType type) {
}
