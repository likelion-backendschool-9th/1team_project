package com.dipsource.entity.demo;

/** 데모 실행 방식 */
public enum DemoType {
    /** 업로드한 zip(html/css/js)을 그대로 실행 */
    STATIC,
    /** 등록한 API를 OpenAPI 명세·시나리오로 실행 */
    API,
    /** 외부 URL로 실행 */
    EXTERNAL
}
