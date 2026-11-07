package com.dipsource.dto.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AdminReportResponse(
        Long id,
        String targetType,
        String targetTitle,
        String targetHref,
        String reason,
        String detail,
        String reporter,
        String reported,
        String createdAt,
        String status) {

    public static AdminReportResponse of(
            Long id,
            String targetType,
            String targetTitle,
            String targetHref,
            String reason,
            String detail,
            String reporter,
            String reported,
            LocalDateTime createdAt,
            String status) {
        return new AdminReportResponse(
                id,
                targetType,
                clip(targetTitle),
                targetHref,
                reason,
                detail == null ? "" : detail,
                reporter,
                reported,
                createdAt == null ? null : createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                status);
    }

    private static String clip(String text) {
        String value = text == null ? "" : text;
        return value.length() <= 50 ? value : value.substring(0, 50) + "...";
    }
}
