package com.dipsource.service.post;

import com.dipsource.dto.post.PostSearchItemResponse;
import com.dipsource.dto.search.SearchCondition;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 블로그 글 검색. 통합 검색(search)에서 쓴다. */
public interface PostSearchService {

    /**
     * 공개된 글을 검색 조건으로 검색한다.
     * 공개 범위(숨김·삭제 제외)와 유료 글 본문 보호는 이 메서드가 책임진다.
     *
     * @param condition 정리된 검색어, and/or, 정렬
     */
    PageResponse<PostSearchItemResponse> search(SearchCondition condition, Pageable pageable);
}
