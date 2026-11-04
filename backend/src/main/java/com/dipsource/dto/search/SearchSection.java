package com.dipsource.dto.search;

import com.dipsource.global.common.PageResponse;
import java.util.List;

/**
 * 전체 검색에서 대상 하나의 미리보기.
 *
 * @param content       상위 몇 건
 * @param totalElements 이 대상의 전체 검색 결과 수. 탭 옆 숫자로 쓴다
 */
public record SearchSection<T>(List<T> content, long totalElements) {

    public static <T> SearchSection<T> from(PageResponse<T> page) {
        return new SearchSection<>(page.content(), page.totalElements());
    }
}
