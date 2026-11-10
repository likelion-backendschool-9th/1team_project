package com.dipsource.entity.report;

/** 신고 처리 상태 */
public enum ReportStatus {
    /** 처리 대기 */
    PENDING,
    /** 조치 완료 */
    RESOLVED,
    /** 반려 */
    REJECTED
}
