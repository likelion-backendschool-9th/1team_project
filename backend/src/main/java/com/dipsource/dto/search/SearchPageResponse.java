package com.dipsource.dto.search;

import com.dipsource.global.common.PageResponse;
import java.util.List;

/**
 * 대상 하나 검색(type=post 등) 응답. 공통 페이지 응답과 같은 필드에 correctedQuery, suggestedQuery만 더한다.
 * 둘은 함께 오지 않는다.
 *
 * @param correctedQuery 0건이라 오타를 고쳐서 다시 검색했을 때의 검색어. 결과도 고친 검색어 기준이다. 없으면 null(응답에서 빠짐)
 * @param suggestedQuery 결과가 적어서 제안하는 검색어. 결과는 원래 검색어 기준 그대로다. 없으면 null(응답에서 빠짐)
 */
public record SearchPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String correctedQuery,
        String suggestedQuery
) {

    public static <T> SearchPageResponse<T> of(PageResponse<T> page) {
        return new SearchPageResponse<>(page.content(), page.page(), page.size(),
                page.totalElements(), page.totalPages(), null, null);
    }

    public static <T> SearchPageResponse<T> corrected(PageResponse<T> page, String correctedQuery) {
        return new SearchPageResponse<>(page.content(), page.page(), page.size(),
                page.totalElements(), page.totalPages(), correctedQuery, null);
    }

    public SearchPageResponse<T> withSuggestedQuery(String suggestedQuery) {
        return new SearchPageResponse<>(content, page, size, totalElements, totalPages, correctedQuery, suggestedQuery);
    }
}
