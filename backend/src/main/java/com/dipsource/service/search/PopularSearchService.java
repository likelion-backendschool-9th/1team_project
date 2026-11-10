package com.dipsource.service.search;

import java.util.List;

/** 인기 검색어. 검색이 끝날 때 기록을 남기고, 최근 검색이 많은 순으로 돌려준다. */
public interface PopularSearchService {

    /** 검색어를 정규화해 기록한다. 너무 짧거나 길면, 또는 같은 사용자의 10분 안 반복이면 남기지 않는다. */
    void record(String query, Long userId);

    /** 최근 7일 인기 검색어. 기록이 없으면 공개 콘텐츠에 많이 붙은 태그 이름. */
    List<String> popular(int limit);
}
