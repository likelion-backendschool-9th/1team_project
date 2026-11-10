package com.dipsource.dto.demo;

/** 버전의 Mock 설정·샘플 데이터. editor는 코드 편집기로 만든 버전에만 있다. */
public record DemoVersionConfigResponse(String mockConfig, String sampleData, DemoEditorSource editor) {
}
