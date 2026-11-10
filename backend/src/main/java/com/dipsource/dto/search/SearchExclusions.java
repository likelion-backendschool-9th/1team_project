package com.dipsource.dto.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 검색에서 뺄 사용자별 대상. 비로그인이면 {@link #NONE}이다.
 *
 * @param hidden  검색 대상별로 사용자가 숨긴 문서 ID
 * @param blocked 차단 관계인 회원 ID
 */
public record SearchExclusions(Map<SearchType, List<Long>> hidden, Set<Long> blocked) {

    public static final SearchExclusions NONE = new SearchExclusions(Map.of(), Set.of());

    public Long[] hiddenIds(SearchType type) {
        return hidden.getOrDefault(type, List.of()).toArray(Long[]::new);
    }

    public Long[] blockedIds() {
        return blocked.toArray(Long[]::new);
    }
}
