package com.dipsource.service.tag;

import com.dipsource.dto.search.AutocompletePrefix;
import com.dipsource.dto.search.SearchTermCandidate;
import com.dipsource.dto.search.SearchTermFrequency;
import java.util.List;

/** 통합 검색(search)에서 쓰는 태그 조회. */
public interface TagSearchService {

    /**
     * 오타 교정 후보. 태그 이름에 나오는 단어 중 검색 단어와 가까운 단어 두 개(가까운 순).
     *
     * @param word        소문자 검색 단어
     * @param maxDistance 허용하는 최대 편집 거리
     */
    List<SearchTermCandidate> findClosestTerms(String word, int maxDistance);

    /**
     * 자동완성 단어 후보. 태그 이름에 나오는 단어 중 접두어로 시작하는 단어와 등장 횟수(자주 나온 순).
     *
     * @param prefix 마지막 단어의 접두어 조건
     * @param limit  최대 개수
     */
    List<SearchTermFrequency> findTermsByPrefix(AutocompletePrefix prefix, int limit);

    /**
     * 자동완성 태그 후보. 입력 전체로 시작하는 태그 이름(짧은 순).
     *
     * @param prefix 입력 전체의 접두어 조건
     * @param limit  최대 개수
     */
    List<String> findNamesByPrefix(AutocompletePrefix prefix, int limit);
}
