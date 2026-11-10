package com.dipsource.dto.demo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 코드 편집기 저장 요청. mockConfig·sampleData는 JSON 문자열이고 비워 둘 수 있다. */
public record DemoEditorSaveRequest(
        @NotNull(message = "HTML을 입력해 주세요") @Size(max = 262144, message = "HTML은 256KB 이하여야 합니다") String html,
        @Size(max = 262144, message = "CSS는 256KB 이하여야 합니다") String css,
        @Size(max = 262144, message = "JavaScript는 256KB 이하여야 합니다") String js,
        @Size(max = 131072, message = "Mock 설정은 128KB 이하여야 합니다") String mockConfig,
        @Size(max = 262144, message = "샘플 데이터는 256KB 이하여야 합니다") String sampleData
) {
}
