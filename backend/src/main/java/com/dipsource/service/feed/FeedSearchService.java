package com.dipsource.service.feed;

import com.dipsource.dto.feed.FeedSearchItemResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 피드 검색. 통합 검색(search)에서 쓴다. 피드 CRUD 서비스와 따로 둔다. */
public interface FeedSearchService {

    /**
     * 공개된 피드를 키워드로 검색한다. 숨김·삭제된 피드는 빠진다.
     *
     * @param keyword 앞뒤 공백을 제거한, 비어 있지 않은 검색어
     */
    PageResponse<FeedSearchItemResponse> search(String keyword, Pageable pageable);
}
