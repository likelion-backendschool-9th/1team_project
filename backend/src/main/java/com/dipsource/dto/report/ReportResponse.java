package com.dipsource.dto.report;

import com.dipsource.entity.report.Report;
import java.time.LocalDateTime;

/**
 * 신고 응답 DTO
 */
public record ReportResponse(
        Long id,
        Long reporterId,
        String targetType,
        Long targetId,
        String reason,
        String status,
        LocalDateTime createdAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporterId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getStatus().name(),
                report.getCreatedAt()
        );
    }
}
