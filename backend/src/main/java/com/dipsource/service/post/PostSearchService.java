package com.dipsource.service.post;

import com.dipsource.dto.post.PostSearchItemResponse;
import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchTermCandidate;
import com.dipsource.global.common.PageResponse;
import java.util.Optional;
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

    /**
     * 오타 교정 후보. 공개 글 제목에 나오는 단어 중 검색 단어와 가장 가까운 단어.
     *
     * @param word        소문자 검색 단어
     * @param maxDistance 허용하는 최대 편집 거리
     */
    Optional<SearchTermCandidate> findClosestTerm(String word, int maxDistance);
}
