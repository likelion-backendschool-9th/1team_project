package com.dipsource.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCache;

class CacheConfigTest {

    @Test
    void onlyNewsCacheExpiresAfterTenMinutes() {
        var manager = new CacheConfig().cacheManager();

        assertThat(manager.getCacheNames()).containsExactly(CacheConfig.NEWS);
        assertThat(manager.getCache("users")).isNull();

        var cache = (CaffeineCache) manager.getCache(CacheConfig.NEWS);
        long minutes = cache.getNativeCache()
                .policy()
                .expireAfterWrite()
                .orElseThrow()
                .getExpiresAfter(TimeUnit.MINUTES);
        assertThat(minutes).isEqualTo(10);
    }
}
