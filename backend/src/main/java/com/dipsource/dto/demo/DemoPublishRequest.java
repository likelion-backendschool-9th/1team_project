package com.dipsource.dto.demo;

/**
 * 버전 공개·복구 요청.
 *
 * @param versionId                공개할 버전. 없으면 가장 최근 READY 버전
 * @param expectedCurrentVersionId 화면에서 보고 있던 현재 공개 버전. 보내면 그 사이 공개 버전이 바뀐 경우 409를 준다
 */
public record DemoPublishRequest(Long versionId, Long expectedCurrentVersionId) {
}
