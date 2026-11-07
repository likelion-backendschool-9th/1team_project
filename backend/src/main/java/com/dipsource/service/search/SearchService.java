package com.dipsource.service.search;

import com.dipsource.dto.search.SearchAllResponse;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchPageResponse;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchType;
import org.springframework.data.domain.Pageable;

/** 통합 검색. 검색어를 정리하고 검색 대상(type)별 기능 서비스로 넘긴다. */
public interface SearchService {

    /**
     * 전체 검색. 대상별 상위 몇 건과 총개수. 모든 대상이 0건이면 오타를 고쳐서 한 번 더 검색하고, 합쳐서 몇 건뿐이면 검색어를 제안한다.
     *
     * @param query    사용자가 입력한 검색어(앞뒤 공백 포함 가능)
     * @param operator 여러 단어를 묶는 방식(and/or)
     * @param sort
     * @param viewerId    
     */
    SearchAllResponse searchAll(String query, SearchOperator operator, SearchSort sort, Long viewerId);

    /**
     * 대상 하나를 페이지 단위로 검색한다. 결과가 0건이면 오타를 고쳐서 한 번 더 검색하고, 몇 건뿐이면 검색어를 제안한다.
     *
     * @param query    사용자가 입력한 검색어(앞뒤 공백 포함 가능)
     * @param type     ALL을 뺀 검색 대상
     * @param operator 여러 단어를 묶는 방식(and/or)
     * @param sort     정렬
     * @param viewerId
     */
    SearchPageResponse<?> search(
        String query, 
        SearchType type, 
        SearchOperator operator, 
        SearchSort sort, 
        Pageable pageable,
        Long viewerId
    );
}
