package com.dipsource.service.search.impl;

import com.dipsource.dto.search.SearchAllResponse;
import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchSection;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchType;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.demo.DemoSearchService;
import com.dipsource.service.feed.FeedSearchService;
import com.dipsource.service.post.PostSearchService;
import com.dipsource.service.search.SearchService;
import com.dipsource.service.user.UserSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** {@link SearchService} 구현체. 검색 기술은 모르고, 입력 검증과 대상별 분기만 한다. */
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    /** 검색어 최대 글자 수. 프론트 검색창 제한과 같다 */
    static final int MAX_QUERY_LENGTH = 50;
    /** 한 페이지 최대 건수 */
    static final int MAX_PAGE_SIZE = 50;
    /** 전체 검색에서 대상별로 미리 보여 줄 건수 */
    static final int PREVIEW_SIZE = 5;

    private final PostSearchService postSearchService;
    private final FeedSearchService feedSearchService;
    private final DemoSearchService demoSearchService;
    private final UserSearchService userSearchService;

    @Override
    public SearchAllResponse searchAll(String query, SearchOperator operator, SearchSort sort) {
        SearchCondition condition = SearchCondition.of(normalize(query), operator, sort);
        Pageable preview = PageRequest.of(0, PREVIEW_SIZE);
        return new SearchAllResponse(
                SearchSection.from(postSearchService.search(condition, preview)),
                SearchSection.from(feedSearchService.search(condition, preview)),
                SearchSection.from(demoSearchService.search(condition, preview)),
                SearchSection.from(userSearchService.search(condition, preview))
        );
    }

    @Override
    public PageResponse<?> search(String query, SearchType type, SearchOperator operator, SearchSort sort,
                                  Pageable pageable) {
        SearchCondition condition = SearchCondition.of(normalize(query), operator, sort);
        // 정렬은 SearchSort로 받으므로 Pageable에 들어온 정렬은 쓰지 않는다
        Pageable page = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE));

        return switch (type) {
            case POST -> postSearchService.search(condition, page);
            case FEED -> feedSearchService.search(condition, page);
            case DEMO -> demoSearchService.search(condition, page);
            case USER -> userSearchService.search(condition, page);
            case ALL -> throw new IllegalArgumentException("전체 검색은 searchAll을 쓴다");
        };
    }

    private static String normalize(String query) {
        String keyword = query == null ? "" : query.strip();
        if (keyword.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "검색어를 입력해 주세요");
        }
        if (keyword.length() > MAX_QUERY_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "검색어는 " + MAX_QUERY_LENGTH + "자 이하로 입력해 주세요");
        }
        return keyword;
    }
}
