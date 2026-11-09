package com.dipsource.service.demo.mock;

/**
 * 검증을 마친 Mock 설정.
 *
 * @param mockConfig 설정 원문. 없으면 null
 * @param sampleData 샘플 데이터 원문. 없으면 null
 * @param json       Worker가 읽을 mock.json. 설정이 없으면 null
 */
public record CompiledMock(String mockConfig, String sampleData, byte[] json) {
}
