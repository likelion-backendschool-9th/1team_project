package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoSearchItemResponse;
import com.dipsource.dto.search.AutocompletePrefix;
import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchTermCandidate;
import com.dipsource.dto.search.SearchTermFrequency;
import com.dipsource.global.common.PageResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

/** 데모 검색. 통합 검색(search)에서 쓴다. 데모 CRUD 서비스와 따로 둔다. */
public interface DemoSearchService {

    /**
     * 공개된 데모를 검색 조건으로 검색한다. 숨김·삭제된 데모는 빠진다.
     *
     * @param condition 정리된 검색어, and/or, 정렬
     */
    PageResponse<DemoSearchItemResponse> search(SearchCondition condition, Pageable pageable);

    /**
     * 오타 교정 후보. 공개 데모 제목에 나오는 단어 중 검색 단어와 가까운 단어 두 개(가까운 순).
     *
     * @param word        소문자 검색 단어
     * @param maxDistance 허용하는 최대 편집 거리
     */
    List<SearchTermCandidate> findClosestTerms(String word, int maxDistance);

    /**
     * 자동완성 단어 후보. 공개 데모 제목에 나오는 단어 중 접두어로 시작하는 단어와 등장 횟수(자주 나온 순).
     *
     * @param prefix 마지막 단어의 접두어 조건
     * @param limit  최대 개수
     */
    List<SearchTermFrequency> findTermsByPrefix(AutocompletePrefix prefix, int limit);
}
