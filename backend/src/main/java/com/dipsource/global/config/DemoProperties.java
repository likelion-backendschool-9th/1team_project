package com.dipsource.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 데모 플랫폼 설정 (application.yml의 demo.*).
 *
 * @param bucket        데모 정적 파일을 두는 비공개 R2 버킷
 * @param workerBaseUrl 데모 실행 Worker 주소. 끝에 / 없이 쓴다
 * @param limits        업로드 ZIP 제한
 */
@ConfigurationProperties(prefix = "demo")
public record DemoProperties(String bucket, String workerBaseUrl, Limits limits) {

    /**
     * @param maxZipBytes   ZIP 자체 크기
     * @param maxTotalBytes 압축 해제 후 총 크기
     * @param maxFileBytes  파일 하나의 크기
     * @param maxFiles      파일 수
     */
    public record Limits(long maxZipBytes, long maxTotalBytes, long maxFileBytes, int maxFiles) {
    }
}
