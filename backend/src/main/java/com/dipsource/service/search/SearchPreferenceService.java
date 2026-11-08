package com.dipsource.service.search;

import com.dipsource.dto.search.SearchHiddenItemResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 사용자별 검색 설정: 결과 숨김, 방문 기록. */
public interface SearchPreferenceService {

    void hide(Long userId, String targetType, Long targetId);

    void unhide(Long userId, String targetType, Long targetId);

    PageResponse<SearchHiddenItemResponse> hiddenList(Long userId, Pageable pageable);

    void recordVisit(Long userId, String targetType, Long targetId, String query);
}
