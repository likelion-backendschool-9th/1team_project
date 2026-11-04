package com.dipsource.service.news.impl;

import com.dipsource.global.config.CacheConfig;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/** 검색어 없는 전체 목록만 10분 캐시한다. 성공한 목록은 만료 저장소에도 넣는다. */
@Component
@RequiredArgsConstructor
public class CachingNewsFeed implements NewsFeedSource {

    private final GeekNewsClient client;
    private final LastNewsSnapshot snapshot;

    @Override
    @Cacheable(cacheNames = CacheConfig.NEWS, key = "'all'", sync = true)
    public List<NewsItemDraft> load() {
        List<NewsItemDraft> items = client.fetch();
        if (items.isEmpty()) {
            throw new IllegalStateException("RSS에 글이 없습니다");
        }
        snapshot.save(items);
        return items;
    }
}
