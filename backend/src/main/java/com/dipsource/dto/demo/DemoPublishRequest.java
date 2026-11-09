package com.dipsource.dto.demo;

/**
 * 버전 공개 요청.
 *
 * @param versionId 공개할 버전. 없으면 가장 최근 READY 버전
 */
public record DemoPublishRequest(Long versionId) {
}
