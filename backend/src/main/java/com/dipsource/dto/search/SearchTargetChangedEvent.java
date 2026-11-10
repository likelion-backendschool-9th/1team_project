package com.dipsource.dto.search;

/** JPA 이벤트로 잡히지 않는 변경(벌크 쿼리)을 검색 색인에 알린다. 검색 색인이 꺼져 있으면 받는 곳이 없다. */
public record SearchTargetChangedEvent(SearchType type, long id) {
}
