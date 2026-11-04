package com.dipsource.dto.search;

import com.dipsource.global.common.PageResponse;
import java.util.List;

/**
 * 대상 하나 검색(type=post 등) 응답. 공통 페이지 응답과 같은 필드에 correctedQuery만 더한다.
 *
 * @param correctedQuery 오타를 고쳐서 다시 검색했을 때의 검색어. 고치지 않았으면 null(응답에서 빠짐)
 */
public record SearchPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String correctedQuery
) {

    public static <T> SearchPageResponse<T> of(PageResponse<T> page, String correctedQuery) {
        return new SearchPageResponse<>(page.content(), page.page(), page.size(),
                page.totalElements(), page.totalPages(), correctedQuery);
    }
}
