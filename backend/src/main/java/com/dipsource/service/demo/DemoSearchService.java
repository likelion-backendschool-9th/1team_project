package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoSearchItemResponse;
import com.dipsource.dto.search.SearchCondition;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 데모 검색. 통합 검색(search)에서 쓴다. 데모 CRUD 서비스와 따로 둔다. */
public interface DemoSearchService {

    /**
     * 공개된 데모를 검색 조건으로 검색한다. 숨김·삭제된 데모는 빠진다.
     *
     * @param condition 정리된 검색어, and/or, 정렬
     */
    PageResponse<DemoSearchItemResponse> search(SearchCondition condition, Pageable pageable);
}
