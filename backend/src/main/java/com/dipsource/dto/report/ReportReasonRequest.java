package com.dipsource.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 신고 사유 요청 DTO
 * POST /feeds/{feedId}/report, /posts/{postId}/report, /demos/{demoId}/report 에서 사용
 * targetType과 targetId는 URL 경로에서 추출하고, body에는 reason만 받는다.
 */
public record ReportReasonRequest(

        /** 신고 사유 */
        @NotBlank @Size(max = 500) String reason
) {}
