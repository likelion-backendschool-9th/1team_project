package com.dipsource.service.post.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * 조회수를 올릴지 정한다. 로그인 회원은 같은 글을 한 시간에 한 번만 센다(새로고침으로 부풀리지 않게).
 * 비로그인은 프론트 서버를 거쳐 들어와 사람을 구분할 수 없어서 매번 센다.
 * 서버 메모리에 두므로 재시작하면 초기화된다.
 */
@Component
public class PostViewCounter {

    /** 같은 회원이 같은 글을 다시 세기까지 걸리는 시간 */
    static final Duration WINDOW = Duration.ofHours(1);

    /** 기억해 둘 (회원, 글) 쌍의 최대 개수. 넘으면 오래된 것부터 지운다 */
    private static final long MAX_ENTRIES = 100_000;

    private final Cache<String, Boolean> recent = Caffeine.newBuilder()
            .expireAfterWrite(WINDOW)
            .maximumSize(MAX_ENTRIES)
            .build();

    /** 이번 조회를 셀지. 비로그인(viewerId null)은 항상 true */
    public boolean shouldCount(Long postId, Long viewerId) {
        if (viewerId == null) {
            return true;
        }
        // 처음 넣을 때만 null이 돌아온다 → 한 시간 안의 두 번째부터는 세지 않는다.
        return recent.asMap().putIfAbsent(viewerId + ":" + postId, Boolean.TRUE) == null;
    }
}
