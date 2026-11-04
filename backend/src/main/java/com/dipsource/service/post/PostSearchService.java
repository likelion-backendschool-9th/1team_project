package com.dipsource.service.post;

import com.dipsource.dto.post.PostSearchItemResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 블로그 글 검색. 통합 검색(search)에서 쓴다. */
public interface PostSearchService {

    /**
     * 공개된 글을 키워드로 검색한다.
     * 공개 범위(숨김·삭제 제외)와 유료 글 본문 보호는 이 메서드가 책임진다.
     *
     * @param keyword 앞뒤 공백을 제거한, 비어 있지 않은 검색어
     */
    PageResponse<PostSearchItemResponse> search(String keyword, Pageable pageable);
}
