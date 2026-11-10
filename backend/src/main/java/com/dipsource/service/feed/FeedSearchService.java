package com.dipsource.service.feed;

import com.dipsource.dto.feed.FeedSearchItemResponse;
import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchSignal;
import com.dipsource.global.common.PageResponse;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;

/** 피드 검색. 통합 검색(search)에서 쓴다. 피드 CRUD 서비스와 따로 둔다. */
public interface FeedSearchService {

    /**
     * 공개된 피드를 검색 조건으로 검색한다. 숨김·삭제된 피드는 빠진다.
     *
     * @param condition 정리된 검색어, and/or, 정렬
     */
    PageResponse<FeedSearchItemResponse> search(SearchCondition condition, Pageable pageable);

    /** 재정렬 신호. 공개 상태가 아닌 피드은(는) 빠진다 */
    List<SearchSignal> findSignals(Collection<Long> ids);

    /** 검색 결과 항목을 주어진 ID 순서대로 만든다 */
    List<FeedSearchItemResponse> findItems(List<Long> ids);
}
