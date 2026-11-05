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
import java.util.function.UnaryOperator;
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
 *
 * <p>검색어 제안(3단계): 결과가 1건 이상 {@value #SUGGEST_BELOW_TOTAL}건 미만이면 결과는 그대로 두고 suggestedQuery만 붙인다.
 * sprng로 쓴 글이 있어서 sprng 검색 결과가 0건이 아니어도, 사전에서 spring이 훨씬 자주 나오면 spring을 제안한다.
 * 제안한 검색어로 결과가 있을 때만 붙인다.
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
    /** 결과 총개수가 이 값보다 적으면(0건 제외) 검색어 제안을 찾는다. 모든 검색에 붙이면 사전 조회 비용이 너무 크다 */
    static final int SUGGEST_BELOW_TOTAL = 5;
    /** 제안 단어는 검색 단어보다 사전에 이 배수 이상 자주 나와야 한다. 오타보다 바른 철자가 훨씬 흔하다는 가정 */
    static final int SUGGEST_FREQUENCY_RATIO = 3;

    private final PostSearchService postSearchService;
    private final FeedSearchService feedSearchService;
    private final DemoSearchService demoSearchService;
    private final UserSearchService userSearchService;
    private final TagSearchService tagSearchService;

    @Override
    public SearchAllResponse searchAll(String query, SearchOperator operator, SearchSort sort) {
        SearchCondition condition = SearchCondition.of(normalize(query), operator, sort);
        SearchAllResponse result = searchAll(condition, null);
        long total = total(result);
        if (total == 0) {
            return correct(condition)
                    .map(corrected -> searchAll(corrected, corrected.phrase()))
                    .filter(corrected -> total(corrected) > 0)
                    .orElse(result);
        }
        if (total >= SUGGEST_BELOW_TOTAL) {
            return result;
        }
        return suggest(condition)
                .filter(suggested -> total(searchAll(suggested, null)) > 0)
                .map(suggested -> result.withSuggestedQuery(suggested.phrase()))
                .orElse(result);
    }

    @Override
    public SearchPageResponse<?> search(String query, SearchType type, SearchOperator operator, SearchSort sort,
                                        Pageable pageable) {
        SearchCondition condition = SearchCondition.of(normalize(query), operator, sort);
        // 정렬은 SearchSort로 받으므로 Pageable에 들어온 정렬은 쓰지 않는다
        Pageable page = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE));

        SearchPageResponse<?> result = SearchPageResponse.of(search(condition, type, page));
        long total = result.totalElements();
        if (total == 0) {
            return correct(condition)
                    .<SearchPageResponse<?>>map(corrected ->
                            SearchPageResponse.corrected(search(corrected, type, page), corrected.phrase()))
                    .filter(corrected -> corrected.totalElements() > 0)
                    .orElse(result);
        }
        if (total >= SUGGEST_BELOW_TOTAL) {
            return result;
        }
        // 총개수만 보면 되므로 한 건만 가져온다
        return suggest(condition)
                .filter(suggested -> search(suggested, type, PageRequest.of(0, 1)).totalElements() > 0)
                .<SearchPageResponse<?>>map(suggested -> result.withSuggestedQuery(suggested.phrase()))
                .orElse(result);
    }

    private SearchAllResponse searchAll(SearchCondition condition, String correctedQuery) {
        Pageable preview = PageRequest.of(0, PREVIEW_SIZE);
        return new SearchAllResponse(
                SearchSection.from(postSearchService.search(condition, preview)),
                SearchSection.from(feedSearchService.search(condition, preview)),
                SearchSection.from(demoSearchService.search(condition, preview)),
                SearchSection.from(userSearchService.search(condition, preview)),
                correctedQuery,
                null
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

    private static long total(SearchAllResponse result) {
        return result.posts().totalElements() + result.feeds().totalElements()
                + result.demos().totalElements() + result.users().totalElements();
    }

    /** 단어마다 오타를 고친 조건. 고친 단어가 하나도 없으면 비어 있다. */
    private Optional<SearchCondition> correct(SearchCondition condition) {
        return rewrite(condition, this::correctWord);
    }

    /** 단어마다 제안 단어로 바꾼 조건. 바꾼 단어가 하나도 없으면 비어 있다. */
    private Optional<SearchCondition> suggest(SearchCondition condition) {
        return rewrite(condition, this::suggestWord);
    }

    private static Optional<SearchCondition> rewrite(SearchCondition condition, UnaryOperator<String> rewriteWord) {
        List<String> rewritten = condition.keywords().stream().map(rewriteWord).toList();
        if (rewritten.equals(condition.keywords())) {
            return Optional.empty();
        }
        return Optional.of(condition.withKeywords(rewritten));
    }

    /**
     * 태그, 공개 글 제목, 공개 데모 제목에서 가장 가까운 단어로 바꾼다.
     * 가까운 단어가 없거나, 검색 단어를 그대로 포함하는 단어가 있으면(오타가 아님) 그대로 둔다.
     */
    private String correctWord(String word) {
        return candidates(word).stream()
                .min(SearchTermCandidate.BEST_FIRST)
                .filter(candidate -> !candidate.getContained())
                .map(SearchTermCandidate::getTerm)
                .orElse(word);
    }

    /**
     * 검색 단어보다 사전에 {@value #SUGGEST_FREQUENCY_RATIO}배 이상 자주 나오는 가까운 단어로 바꾼다.
     * 검색 단어를 포함하는 다른 단어(jav → java)가 있으면 이미 그 단어로도 찾히므로 그대로 둔다.
     * 등장 횟수는 사전 출처(태그·글 제목·데모 제목)별로 센 값이라, 출처를 합친 횟수는 아니다.
     */
    private String suggestWord(String word) {
        List<SearchTermCandidate> candidates = candidates(word);
        if (candidates.stream().anyMatch(candidate -> candidate.getContained() && !candidate.getTerm().equals(word))) {
            return word;
        }
        long ownFrequency = candidates.stream()
                .filter(candidate -> candidate.getTerm().equals(word))
                .mapToLong(SearchTermCandidate::getFrequency)
                .sum();
        return candidates.stream()
                .filter(candidate -> !candidate.getContained())
                .min(SearchTermCandidate.BEST_FIRST)
                .filter(candidate -> candidate.getFrequency() >= ownFrequency * SUGGEST_FREQUENCY_RATIO)
                .map(SearchTermCandidate::getTerm)
                .orElse(word);
    }

    /** 태그, 공개 글 제목, 공개 데모 제목에서 찾은 가까운 단어들. 짧은 단어는 찾지 않는다 */
    private List<SearchTermCandidate> candidates(String word) {
        int maxDistance = maxDistance(word);
        if (maxDistance == 0) {
            return List.of();
        }
        return Stream.of(
                        tagSearchService.findClosestTerms(word, maxDistance),
                        postSearchService.findClosestTerms(word, maxDistance),
                        demoSearchService.findClosestTerms(word, maxDistance))
                .flatMap(List::stream)
                .toList();
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
