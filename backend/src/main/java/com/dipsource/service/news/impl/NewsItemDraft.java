package com.dipsource.service.news.impl;

import java.time.OffsetDateTime;

/** 캐시에 넣는 뉴스. id는 필터 뒤에 응답에서만 붙인다. */
public record NewsItemDraft(
        String title,
        String summary,
        String source,
        String category,
        OffsetDateTime publishedAt,
        String url
) {
}
