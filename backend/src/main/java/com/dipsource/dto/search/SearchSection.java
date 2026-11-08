package com.dipsource.dto.search;

import com.dipsource.global.common.PageResponse;
import java.util.List;

/**
 * 전체 검색에서 대상 하나의 미리보기.
 *
 * @param content       상위 몇 건
 * @param totalElements 이 대상의 전체 검색 결과 수. 탭 옆 숫자로 쓴다
 * @param relaxedMatch  정확히 일치하는 결과가 없어서 검색어로 시작하는 단어의 결과를 보여 줄 때 true. 아니면 null
 */
public record SearchSection<T>(List<T> content, long totalElements, Boolean relaxedMatch) {

    public SearchSection(List<T> content, long totalElements) {
        this(content, totalElements, null);
    }

    public static <T> SearchSection<T> from(PageResponse<T> page) {
        return new SearchSection<>(page.content(), page.totalElements());
    }
}
