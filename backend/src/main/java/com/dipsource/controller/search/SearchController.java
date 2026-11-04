package com.dipsource.controller.search;

import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchType;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 통합 검색 API. 비로그인도 쓸 수 있다. */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 검색. 예: GET /api/v1/search?q=spring jpa&type=post&sort=relevance&op=and&page=0&size=20
     * - q: 검색어. 앞뒤 공백 제거 후 1~50자
     * - type: all(기본값), post, feed, demo, user
     *   - all: 대상별 상위 5건 + 총개수 (page, size 무시)
     *   - 그 외: 해당 대상의 페이지 응답
     * - sort: relevance(기본값, 관련도순), latest(최신순)
     * - op: 검색어가 여러 단어일 때 단어끼리 묶는 방식(operator). 단어는 공백으로 나누고 최대 5개까지 쓴다
     *   - and(기본값): 모든 단어가 들어 있는 결과. 예: "spring jpa" → spring과 jpa가 둘 다 있는 글
     *   - or: 하나라도 들어 있는 결과. 많이 일치할수록 relevance 점수가 높다
     * - page, size: 0부터 시작, size는 최대 50
     */
    @GetMapping
    public ApiResponse<?> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String op,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        SearchType searchType = SearchType.from(type);
        SearchSort searchSort = SearchSort.from(sort);
        SearchOperator operator = SearchOperator.from(op);
        if (searchType == SearchType.ALL) {
            return ApiResponse.of(searchService.searchAll(q, operator, searchSort));
        }
        return ApiResponse.of(searchService.search(q, searchType, operator, searchSort, pageable));
    }
}
