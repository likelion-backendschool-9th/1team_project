package com.dipsource.service.search;

import com.dipsource.dto.search.AutocompleteResponse;
import java.util.List;

/** 검색어 자동완성. 검색창에 입력하는 중에 완성될 검색어 후보를 준다. */
public interface AutocompleteService {

    /**
     * 입력으로 시작하는 태그와, 마지막 단어를 완성한 검색어 후보.
     * 순서: 입력과 똑같은 후보 → 태그 → 자주 나온 단어 → 짧은 단어. 같은 검색어는 한 번만 준다.
     *
     * @param query 사용자가 입력 중인 검색어. 비어 있으면 빈 목록
     */
    List<AutocompleteResponse> autocomplete(String query);
}
