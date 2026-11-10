package com.dipsource.dto.search;

import com.dipsource.global.common.PageResponse;
import java.util.List;

/**
 * 대상 하나 검색(type=post 등) 응답. 공통 페이지 응답과 같은 필드에 correctedQuery, suggestedQuery만 더한다.
 * 둘은 함께 오지 않는다.
 *
 * @param correctedQuery 0건이라 오타를 고쳐서 다시 검색했을 때의 검색어. 결과도 고친 검색어 기준이다. 없으면 null(응답에서 빠짐)
 * @param suggestedQuery 제안하는 검색어. 결과는 원래 검색어 기준 그대로다. 없으면 null(응답에서 빠짐)
 * @param suggestionType 제안 이유(KEYBOARD, TYPO). 없으면 null
 * @param relaxedMatch   모든 단어가 일치하는 결과가 없어서 일부 단어만 일치하는 결과를 보여 줄 때 true. 아니면 null
 * @param searchId       다음 페이지를 요청할 때 그대로 보내면 같은 순서를 유지한다. PostgreSQL 경로에서는 null
 */
public record SearchPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String correctedQuery,
        String suggestedQuery,
        SuggestionType suggestionType,
        Boolean relaxedMatch,
        String searchId
) {

    public static <T> SearchPageResponse<T> of(PageResponse<T> page) {
        return new SearchPageResponse<>(page.content(), page.page(), page.size(),
                page.totalElements(), page.totalPages(), null, null, null, null, null);
    }

    public static <T> SearchPageResponse<T> corrected(PageResponse<T> page, String correctedQuery) {
        return new SearchPageResponse<>(page.content(), page.page(), page.size(),
                page.totalElements(), page.totalPages(), correctedQuery, null, null, null, null);
    }

    public SearchPageResponse<T> withSuggestedQuery(String suggestedQuery) {
        return new SearchPageResponse<>(content, page, size, totalElements, totalPages, correctedQuery, suggestedQuery,
                suggestionType, relaxedMatch, searchId);
    }
}
