package com.dipsource.service.user;

import com.dipsource.repository.user.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 만료된 Refresh Token 행을 지운다. 교체된 행은 expires_at 전까지 남겨 재사용 감지에 쓴다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 40 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteExpired() {
        LocalDateTime cutoff = LocalDateTime.now(SEOUL).minusDays(1);
        int deleted = refreshTokenRepository.deleteByExpiresAtBefore(cutoff);
        log.info("만료된 refresh token 삭제: {}건", deleted);
    }
}