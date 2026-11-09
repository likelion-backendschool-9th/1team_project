package com.dipsource.entity.demo;

/** 자동 썸네일 상태 */
public enum DemoThumbnailStatus {
    /** 촬영 대기 (재시도 포함) */
    PENDING,
    READY,
    /** 재시도를 다 썼다. 기본 썸네일을 쓴다 */
    FAILED,
    /** 촬영하지 않음 (기능이 꺼져 있거나 데모가 삭제됨) */
    SKIPPED
}
