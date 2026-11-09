package com.dipsource.entity.demo;

/** 데모 버전 상태 */
public enum DemoVersionStatus {
    /** R2로 파일을 보내는 중 */
    UPLOADING,
    /** 검증·저장이 끝나 공개할 수 있음 */
    READY,
    /** 저장 실패. 파일은 정리 대상이다 */
    FAILED,
    /** 작성자가 지움. 정리 작업이 파일을 지우고 용량을 돌려준다 */
    DELETING,
    /** 파일까지 지워짐 */
    DELETED
}
