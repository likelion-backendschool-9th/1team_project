package com.dipsource.dto.demo;

import jakarta.validation.constraints.Size;

/** Mock API·샘플 데이터 저장 요청. 둘 다 JSON 문자열이고 비워 둘 수 있다. */
public record DemoVersionConfigRequest(
        @Size(max = 131072, message = "Mock 설정은 128KB 이하여야 합니다") String mockConfig,
        @Size(max = 262144, message = "샘플 데이터는 256KB 이하여야 합니다") String sampleData
) {
}
