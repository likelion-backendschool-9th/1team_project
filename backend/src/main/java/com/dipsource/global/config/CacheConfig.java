package com.dipsource.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 뉴스 목록 캐시만 10분 만료로 둔다. 다른 캐시 이름은 여기서 만들지 않는다. */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String NEWS = "news";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(NEWS);
        manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)));
        return manager;
    }
}
