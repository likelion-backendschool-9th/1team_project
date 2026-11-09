package com.dipsource.service.search.personal;

import com.dipsource.global.config.SearchProperties;
import com.dipsource.repository.search.SearchVisitRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 보관 기간이 지난 방문 기록을 지운다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchVisitCleanupScheduler {

    private final SearchVisitRepository visitRepository;
    private final SearchProperties properties;

    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteExpired() {
        int deleted = visitRepository.deleteOlderThan(
                LocalDateTime.now().minus(properties.getPersonalization().getVisitRetention()));
        log.info("만료된 검색 방문 기록 삭제: {}건", deleted);
    }
}
