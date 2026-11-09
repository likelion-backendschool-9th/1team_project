package com.dipsource.dto.demo;

import com.dipsource.entity.demo.DemoVersion;
import java.time.LocalDateTime;

/** 데모 버전 응답 */
public record DemoVersionResponse(
        Long id,
        int number,
        String status,
        String entryPath,
        int fileCount,
        long totalBytes,
        boolean current,
        LocalDateTime createdAt
) {
    public static DemoVersionResponse of(DemoVersion version, Long currentVersionId) {
        return new DemoVersionResponse(
                version.getId(),
                version.getNumber(),
                version.getStatus().name(),
                version.getEntryPath(),
                version.getFileCount(),
                version.getTotalBytes(),
                version.getId().equals(currentVersionId),
                version.getCreatedAt());
    }
}
