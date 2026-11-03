package com.dipsource.dto.news;

import java.time.OffsetDateTime;

/** 개발 뉴스 한 건. */
public record NewsItemResponse(
        Long id,
        String title,
        String summary,
        String source,
        String category,
        OffsetDateTime publishedAt,
        String url
) {
}
