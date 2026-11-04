package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoSearchItemResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 데모 검색. 통합 검색(search)에서 쓴다. 데모 CRUD 서비스와 따로 둔다. */
public interface DemoSearchService {

    /**
     * 공개된 데모를 키워드로 검색한다. 숨김·삭제된 데모는 빠진다.
     *
     * @param keyword 앞뒤 공백을 제거한, 비어 있지 않은 검색어
     */
    PageResponse<DemoSearchItemResponse> search(String keyword, Pageable pageable);
}
