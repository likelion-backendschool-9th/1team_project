package com.dipsource.service.search.impl;

import com.dipsource.dto.search.SearchAllResponse;
import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchPageResponse;
import com.dipsource.dto.search.SearchSection;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchTermCandidate;
import com.dipsource.dto.search.SearchType;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.demo.DemoSearchService;
import com.dipsource.service.feed.FeedSearchService;
import com.dipsource.service.post.PostSearchService;
import com.dipsource.service.search.SearchService;
import com.dipsource.service.tag.TagSearchService;
import com.dipsource.service.user.UserSearchService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * {@link SearchService} 구현체. 검색 기술은 모르고, 입력 검증과 대상별 분기, 오타 교정 시점만 정한다.
 *
 * <p>오타 교정(3단계): 결과가 0건이면 검색 단어를 공개된 제목·태그의 가장 가까운 단어로 고쳐서 한 번 더 검색한다.
 * 고친 검색으로 결과가 나오면 그 결과와 correctedQuery를 주고, 그래도 0건이면 원래 결과(0건)를 준다.
 */
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
    private final TagSearchService tagSearchService;

    @Override
    public SearchAllResponse searchAll(String query, SearchOperator operator, SearchSort sort) {
        SearchCondition condition = SearchCondition.of(normalize(query), operator, sort);
        SearchAllResponse result = searchAll(condition, null);
        if (!isEmpty(result)) {
            return result;
        }
        return correct(condition)
                .map(corrected -> searchAll(corrected, corrected.phrase()))
                .filter(corrected -> !isEmpty(corrected))
                .orElse(result);
    }

    @Override
    public SearchPageResponse<?> search(String query, SearchType type, SearchOperator operator, SearchSort sort,
                                        Pageable pageable) {
        SearchCondition condition = SearchCondition.of(normalize(query), operator, sort);
        // 정렬은 SearchSort로 받으므로 Pageable에 들어온 정렬은 쓰지 않는다
        Pageable page = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE));

        PageResponse<?> result = search(condition, type, page);
        if (result.totalElements() > 0) {
            return SearchPageResponse.of(result, null);
        }
        return correct(condition)
                .<SearchPageResponse<?>>map(corrected ->
                        SearchPageResponse.of(search(corrected, type, page), corrected.phrase()))
                .filter(corrected -> corrected.totalElements() > 0)
                .orElseGet(() -> SearchPageResponse.of(result, null));
    }

    private SearchAllResponse searchAll(SearchCondition condition, String correctedQuery) {
        Pageable preview = PageRequest.of(0, PREVIEW_SIZE);
        return new SearchAllResponse(
                SearchSection.from(postSearchService.search(condition, preview)),
                SearchSection.from(feedSearchService.search(condition, preview)),
                SearchSection.from(demoSearchService.search(condition, preview)),
                SearchSection.from(userSearchService.search(condition, preview)),
                correctedQuery
        );
    }

    private PageResponse<?> search(SearchCondition condition, SearchType type, Pageable page) {
        return switch (type) {
            case POST -> postSearchService.search(condition, page);
            case FEED -> feedSearchService.search(condition, page);
            case DEMO -> demoSearchService.search(condition, page);
            case USER -> userSearchService.search(condition, page);
            case ALL -> throw new IllegalArgumentException("전체 검색은 searchAll을 쓴다");
        };
    }

    private static boolean isEmpty(SearchAllResponse result) {
        return result.posts().totalElements() == 0
                && result.feeds().totalElements() == 0
                && result.demos().totalElements() == 0
                && result.users().totalElements() == 0;
    }

    /** 단어마다 오타를 고친 조건. 고친 단어가 하나도 없으면 비어 있다. */
    private Optional<SearchCondition> correct(SearchCondition condition) {
        List<String> corrected = condition.keywords().stream().map(this::correctWord).toList();
        if (corrected.equals(condition.keywords())) {
            return Optional.empty();
        }
        return Optional.of(condition.withKeywords(corrected));
    }

    /**
     * 태그, 공개 글 제목, 공개 데모 제목에서 가장 가까운 단어로 바꾼다.
     * 가까운 단어가 없거나, 검색 단어를 그대로 포함하는 단어가 있으면(오타가 아님) 그대로 둔다.
     */
    private String correctWord(String word) {
        int maxDistance = maxDistance(word);
        if (maxDistance == 0) {
            return word;
        }
        return Stream.of(
                        tagSearchService.findClosestTerm(word, maxDistance),
                        postSearchService.findClosestTerm(word, maxDistance),
                        demoSearchService.findClosestTerm(word, maxDistance))
                .flatMap(Optional::stream)
                .min(SearchTermCandidate.BEST_FIRST)
                .filter(candidate -> !candidate.getContained())
                .map(SearchTermCandidate::getTerm)
                .orElse(word);
    }

    /**
     * 단어 길이에 따라 허용하는 편집 거리. 짧은 단어일수록 한 글자만 바꿔도 전혀 다른 단어가 된다.
     * 1~2글자: 고치지 않음(0), 3~4글자: 1, 5글자 이상: 2
     */
    static int maxDistance(String word) {
        int length = word.codePointCount(0, word.length());
        if (length <= 2) {
            return 0;
        }
        return length <= 4 ? 1 : 2;
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
