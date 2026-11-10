package com.dipsource.dto.feed;

import com.dipsource.dto.user.UserSummaryResponse;
import java.util.List;

/**
 * 피드 카드에 붙는 데모 요약.
 * 프론트 DemoSummary가 읽는 필드만 담는다.
 */
public record FeedDemoResponse(
        Long id,
        String title,
        String description,
        String thumbnail,
        List<String> techTags,
        UserSummaryResponse author,
        int likeCount
) {
}
