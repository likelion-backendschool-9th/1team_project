package com.dipsource.service.tag;

import com.dipsource.dto.search.SearchTermCandidate;
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
}
