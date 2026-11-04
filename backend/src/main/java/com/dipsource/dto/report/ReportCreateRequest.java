package com.dipsource.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 신고 생성 요청 DTO
 * POST /api/v1/reports 에서 사용
 */
public record ReportCreateRequest(

        /** 대상 종류. FEED, POST, DEMO, FEED_COMMENT, GUESTBOOK, USER 등 */
        @NotBlank String targetType,

        /** 대상 ID */
        @NotNull Long targetId,

        /** 신고 사유 */
        @NotBlank @Size(max = 500) String reason
) {}
