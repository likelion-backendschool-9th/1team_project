package com.dipsource.dto.search;

/** 2차 재정렬에 쓰는 PostgreSQL 신호. native 쿼리 결과를 받는 인터페이스 projection이다. */
public interface SearchSignal {

    Long getId();

    /** 작성자 ID. 사용자 검색이면 그 사용자 ID */
    Long getAuthorId();

    Integer getLikeCount();

    /** 조회수. 데모는 체험 수, 피드·사용자는 0 */
    Integer getViewCount();

    /** 작성 후 지난 일수 */
    Double getAgeDays();

    /** 작성자(사용자 검색이면 그 사용자)의 전체 팔로워 수 */
    Long getFollowerCount();
}
