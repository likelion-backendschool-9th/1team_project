package com.dipsource.controller.search;

import com.dipsource.dto.search.AutocompleteResponse;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchType;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.search.AutocompleteService;
import com.dipsource.service.search.PopularSearchService;
import com.dipsource.service.search.SearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 통합 검색 API. 비로그인도 쓸 수 있다. */
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final AutocompleteService autocompleteService;
    private final PopularSearchService popularSearchService;

    /**
     * 검색. 예: GET /search?q=spring jpa&type=post&sort=relevance&op=and&page=0&size=20
     * - q: 검색어. 앞뒤 공백 제거 후 50자 이하. #태그는 해시태그 조건
     * - type: all(기본값), post, feed, demo, user
     *   - all: 대상별 상위 5건 + 총개수 (page, size 무시)
     *   - 그 외: 해당 대상의 페이지 응답
     * - sort: relevance(기본값, 관련도순), latest(최신순)
     * - op: 검색어가 여러 단어일 때 단어끼리 묶는 방식(operator). 단어는 공백으로 나눈다
     *   - 생략(auto): and와 같고, 세 단어 이상의 문장만 75% 일치로 찾는다
     *   - and: 모든 단어가 들어 있는 결과. 예: "spring jpa" → spring과 jpa가 둘 다 있는 글
     *   - or: 하나라도 들어 있는 결과. 많이 일치할수록 relevance 점수가 높다
     * - page, size: 0부터 시작, size는 최대 50. Elasticsearch 경로는 10,000건을 넘는 페이지를 요청하면 400
     * - searchId: 앞 페이지 응답의 searchId. 보내면 페이지를 넘기는 동안 순서가 유지된다
     * - Elasticsearch 경로: 검색어를 자동으로 바꾸지 않는다. 결과는 원래 검색어 기준이고 suggestedQuery와 suggestionType(KEYBOARD, TYPO)으로 제안만 한다
     *   예: q=네갸ㅜㅎ → suggestedQuery "spring", suggestionType KEYBOARD
     * - op를 보내지 않았고 모든 단어가 일치하는 결과가 없으면 일부 단어만 일치하는 결과와 relaxedMatch=true를 준다. op=and는 넓히지 않는다
     * - 한 단어 검색의 결과가 없는 대상은 그 글자로 시작하는 단어의 결과와 relaxedMatch=true를 준다. 전체 검색은 대상별로 판단한다
     * - PostgreSQL 경로(전환 전·장애 시): 0건이면 오타를 고쳐서 다시 검색해 correctedQuery가 붙고, 1~4건이면 suggestedQuery가 붙는다
     */
    @GetMapping
    public ApiResponse<?> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String op,
            @RequestParam(required = false) String searchId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SearchType searchType = SearchType.from(type);
        SearchSort searchSort = SearchSort.from(sort);
        SearchOperator operator = SearchOperator.from(op);
        Long viewerId = principal == null ? null : principal.getUserId();
        if (searchType == SearchType.ALL) {
            return ApiResponse.of(searchService.searchAll(q, operator, searchSort, viewerId));
        }
        return ApiResponse.of(searchService.search(q, searchType, operator, searchSort, pageable, viewerId, searchId));
    }

    /** 최근 7일 인기 검색어. 예: GET /search/popular?limit=10. 기록이 없으면 많이 쓰인 태그 이름. */
    @GetMapping("/popular")
    public ApiResponse<List<String>> popular(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.of(popularSearchService.popular(limit));
    }

    /**
     * 검색어 자동완성. 예: GET /search/autocomplete?q=spr
     * - q: 입력 중인 검색어. 비어 있거나 공백만 있으면 빈 목록, 50자를 넘으면 400
     * - 응답: 최대 8개. text(검색창에 채울 검색어), type(TAG: 태그 이름, WORD: 공개 제목·태그의 단어)
     *   - 태그는 입력 전체로 시작하는 것: "spring b" → Spring Boot
     *   - 단어는 마지막 단어만 완성하고 앞 단어는 그대로: "spring b" → spring boot, spring batch
     *   - 한글 조합 중인 글자도 찾는다: "스플" → 스프링
     */
    @GetMapping("/autocomplete")
    public ApiResponse<List<AutocompleteResponse>> autocomplete(@RequestParam(required = false) String q) {
        return ApiResponse.of(autocompleteService.autocomplete(q));
    }
}
