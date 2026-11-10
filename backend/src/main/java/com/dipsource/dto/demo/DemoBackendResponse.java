package com.dipsource.dto.demo;

import com.dipsource.entity.demo.DemoBackendStatus;
import java.time.LocalDateTime;

/**
 * 외부 백엔드 연결 정보.
 *
 * @param apiBaseUrl           연결 안 했으면 없다
 * @param healthCheckUrl       작성자에게만 준다
 * @param status               헬스체크로 확인한 상태
 * @param lastCheckedAt        마지막으로 결과를 얻은 시각
 * @param healthCheckAvailable 지금 상태 확인을 요청할 수 있는지
 */
public record DemoBackendResponse(
        String apiBaseUrl,
        String healthCheckUrl,
        DemoBackendStatus status,
        LocalDateTime lastCheckedAt,
        boolean healthCheckAvailable
) {
}
