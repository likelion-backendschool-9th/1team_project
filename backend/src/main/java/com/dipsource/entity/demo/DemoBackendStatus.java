package com.dipsource.entity.demo;

/** 외부 백엔드 상태. URL이 등록됐다는 것만으로는 ONLINE이 되지 않는다. */
public enum DemoBackendStatus {
    /** 최근 헬스체크 성공 */
    ONLINE,
    /** 최근 헬스체크 실패 */
    OFFLINE,
    /** 아직 확인하지 않았거나 확인할 수 없음 */
    UNKNOWN,
    /** 외부 백엔드를 연결하지 않음 */
    NOT_CONFIGURED
}
