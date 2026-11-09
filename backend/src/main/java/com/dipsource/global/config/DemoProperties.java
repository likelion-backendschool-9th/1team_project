package com.dipsource.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 데모 플랫폼 설정 (application.yml의 demo.*).
 *
 * @param bucket        데모 정적 파일을 두는 비공개 R2 버킷
 * @param workerBaseUrl 데모 실행 Worker 주소. 끝에 / 없이 쓴다
 * @param signingSecret 미리보기 URL 서명 키. Worker의 DEMO_SIGNING_SECRET과 같아야 한다
 * @param limits        업로드 ZIP 제한
 * @param quota         사용자별 사용량 제한
 * @param preview       임시 미리보기
 * @param thumbnail     자동 썸네일
 */
@ConfigurationProperties(prefix = "demo")
public record DemoProperties(String bucket, String workerBaseUrl, String signingSecret, Limits limits,
                             Quota quota, Preview preview, Thumbnail thumbnail) {

    /**
     * @param maxZipBytes   ZIP 자체 크기
     * @param maxTotalBytes 압축 해제 후 총 크기
     * @param maxFileBytes  파일 하나의 크기
     * @param maxFiles      파일 수
     */
    public record Limits(long maxZipBytes, long maxTotalBytes, long maxFileBytes, int maxFiles) {
    }

    /**
     * @param maxDemos          사용자별 데모 수
     * @param maxStorageBytes   사용자별 총 저장 용량 (버전·임시 미리보기 포함)
     * @param maxVersionsPerDemo 데모별 보관 버전 수
     */
    public record Quota(int maxDemos, long maxStorageBytes, int maxVersionsPerDemo) {
    }

    /**
     * @param ttlMinutes       미리보기 URL과 파일이 유지되는 시간
     * @param maxActivePerUser 사용자별 동시에 유지하는 미리보기 수
     */
    public record Preview(long ttlMinutes, int maxActivePerUser) {
    }

    /**
     * @param enabled   Cloudflare Browser Run 호출 여부
     * @param accountId Cloudflare 계정 ID
     * @param apiToken  Browser Run 권한이 있는 API 토큰
     */
    public record Thumbnail(boolean enabled, String accountId, String apiToken) {
    }
}
