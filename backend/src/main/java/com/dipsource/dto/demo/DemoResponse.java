package com.dipsource.dto.demo;

import com.dipsource.dto.user.UserSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 데모 목록·상세 응답.
 *
 * @param runUrl 실행 주소. 공개 중일 때만 있다
 * @param engine 카드에 보여 줄 대표 기술 (첫 태그)
 */
public record DemoResponse(
        Long id,
        String title,
        String description,
        String thumbnail,
        List<String> techTags,
        UserSummaryResponse author,
        int likeCount,
        int playCount,
        int commentCount,
        LocalDateTime createdAt,
        String runtime,
        double sizeMb,
        boolean liked,
        boolean bookmarked,
        String status,
        Long currentVersionId,
        String runUrl,
        String engine
) {
}
