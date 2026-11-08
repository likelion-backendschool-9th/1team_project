package com.dipsource.service.search.sync;

import com.dipsource.dto.search.SearchType;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.service.search.index.SearchIndexManager;
import com.dipsource.service.search.sync.SearchOutboxRepository.Event;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 색인 대기열을 처리한다. 처리할 때마다 PostgreSQL의 현재 상태를 읽어서 색인하거나 지운다.
 * 실패하면 지수 백오프로 다시 시도한다. 한도를 넘긴 행은 남겨 두고 운영 API로 다시 시도한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class SearchOutboxWorker {

    private static final int BATCH = 200;
    public static final int MAX_ATTEMPTS = 10;
    private static final long BACKOFF_SECONDS = 5;
    private static final Duration PROCESSED_RETENTION = Duration.ofDays(7);

    private final SearchOutboxRepository outbox;
    private final SearchIndexManager indexManager;
    private final SearchProperties properties;
    private final PlatformTransactionManager transactionManager;
    private final MeterRegistry meterRegistry;

    @Scheduled(fixedDelayString = "${search.sync.poll-interval:PT2S}")
    public void poll() {
        if (properties.getElasticsearch().isEnabled()) {
            processBatch();
        }
    }

    /** 대기열 한 묶음을 처리하고 처리한 행 수를 돌려준다 */
    public int processBatch() {
        Integer processed = new TransactionTemplate(transactionManager).execute(status -> {
            List<Event> events = outbox.lockPending(BATCH, MAX_ATTEMPTS);
            events.stream().collect(Collectors.groupingBy(Event::type)).forEach(this::apply);
            return events.size();
        });
        return processed == null ? 0 : processed;
    }

    private void apply(SearchType type, List<Event> events) {
        Map<Long, Long> latest = events.stream().collect(Collectors.toMap(Event::targetId, Event::id, Math::max));
        List<Long> eventIds = events.stream().map(Event::id).toList();
        try {
            SearchIndexManager.SyncResult result = indexManager.sync(type, latest);
            List<Long> failedEvents = events.stream()
                    .filter(event -> result.failedIds().contains(event.targetId()))
                    .map(Event::id).toList();
            List<Long> succeeded = new ArrayList<>(eventIds);
            succeeded.removeAll(failedEvents);
            outbox.markProcessed(succeeded);
            if (!failedEvents.isEmpty()) {
                outbox.markFailed(failedEvents, "문서 색인 실패", BACKOFF_SECONDS);
            }
            afterApplied(type, events, Set.copyOf(result.failedIds()));
        } catch (IOException | RuntimeException e) {
            log.warn("검색 색인 동기화 실패({}건): {}", eventIds.size(), e.toString());
            outbox.markFailed(eventIds, e.toString(), BACKOFF_SECONDS);
        }
    }

    private void afterApplied(SearchType type, List<Event> events, Set<Long> failed) {
        Timer lag = meterRegistry.timer("search.index.lag");
        LocalDateTime now = LocalDateTime.now();
        events.stream().filter(event -> !failed.contains(event.targetId()))
                .forEach(event -> lag.record(Duration.between(event.createdAt(), now)));

        List<Long> targets = events.stream().map(Event::targetId).distinct().toList();
        if (type == SearchType.DEMO) {
            List<Long> feeds = outbox.findFeedIdsByDemoIds(targets);
            if (!feeds.isEmpty()) {
                outbox.enqueue(SearchType.FEED, feeds);
            }
        } else if (type == SearchType.USER) {
            outbox.purgeWithdrawnUsers(targets);
        }
    }

    @Scheduled(cron = "0 10 4 * * *", zone = "Asia/Seoul")
    public void deleteProcessed() {
        int deleted = outbox.deleteProcessedBefore(LocalDateTime.now().minus(PROCESSED_RETENTION));
        log.info("처리 완료된 색인 대기열 삭제: {}건", deleted);
    }
}
