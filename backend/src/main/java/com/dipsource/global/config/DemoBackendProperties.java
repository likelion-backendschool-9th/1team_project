package com.dipsource.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 외부 백엔드 연결 설정 (application.yml의 demo.backend.*).
 *
 * @param healthCheckEnabled    서버가 외부 URL로 헬스체크 요청을 보낼지 여부. 기본은 꺼져 있다
 * @param healthCooldownSeconds 데모별 헬스체크 최소 간격
 * @param healthFreshMinutes    이 시간이 지난 결과는 UNKNOWN으로 보여 준다
 * @param healthMaxConcurrent   서버 인스턴스별 동시 헬스체크 수
 * @param healthTimeoutMs       DNS 조회부터 응답까지 전체 제한 시간
 */
@ConfigurationProperties(prefix = "demo.backend")
public record DemoBackendProperties(boolean healthCheckEnabled, long healthCooldownSeconds, long healthFreshMinutes,
                                    int healthMaxConcurrent, long healthTimeoutMs) {
}
