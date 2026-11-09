package com.dipsource.entity.demo;

/** 데모 상태 */
public enum DemoStatus {
    /** 작성자만 볼 수 있는 초안 (아직 공개하지 않음) */
    DRAFT,
    /** 공개 */
    ACTIVE,
    /** 관리자가 숨김 */
    HIDDEN,
    /** 작성자가 삭제 */
    DELETED
}
