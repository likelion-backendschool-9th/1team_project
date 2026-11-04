package com.dipsource.service.search;

import com.dipsource.dto.search.SearchAllResponse;
import com.dipsource.dto.search.SearchType;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 통합 검색. 검색어를 정리하고 검색 대상(type)별 기능 서비스로 넘긴다. */
public interface SearchService {

    /**
     * 전체 검색. 대상별 상위 몇 건과 총개수.
     *
     * @param query 사용자가 입력한 검색어(앞뒤 공백 포함 가능)
     */
    SearchAllResponse searchAll(String query);

    /**
     * 대상 하나를 페이지 단위로 검색한다.
     *
     * @param query 사용자가 입력한 검색어(앞뒤 공백 포함 가능)
     * @param type  ALL을 뺀 검색 대상
     */
    PageResponse<?> search(String query, SearchType type, Pageable pageable);
}
