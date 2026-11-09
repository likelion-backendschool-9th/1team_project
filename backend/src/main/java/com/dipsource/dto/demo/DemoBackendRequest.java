package com.dipsource.dto.demo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 외부 백엔드 연결 요청.
 *
 * @param apiBaseUrl     예: https://example.com/api
 * @param healthCheckUrl 선택. API Base URL과 같은 출처만 쓸 수 있다
 */
public record DemoBackendRequest(
        @NotBlank(message = "API Base URL을 입력해 주세요") @Size(max = 500, message = "주소는 500자 이하여야 합니다") String apiBaseUrl,
        @Size(max = 500, message = "주소는 500자 이하여야 합니다") String healthCheckUrl
) {
}
