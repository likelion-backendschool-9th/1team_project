package com.dipsource.service.search.sync;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
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
import java.util.HashSet;
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
 * 문서 자체의 실패는 지수 백오프로 다시 시도하고, 한도를 넘긴 행은 남겨 두고 운영 API로 다시 시도한다.
 * 검색 엔진이 죽은 경우는 시도 횟수를 쓰지 않으므로 장애가 길어져도 복구 뒤 자동으로 반영된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class SearchOutboxWorker {

    private static final int BATCH = 200;
    public static final int MAX_ATTEMPTS = 10;
    private static final long BACKOFF_SECONDS = 5;
    private static final long DEFER_SECONDS = 30;
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
            List<Long> deferred = eventsOf(events, result.unavailableIds());
            List<Long> failed = eventsOf(events, result.failedIds());
            List<Long> succeeded = new ArrayList<>(eventIds);
            succeeded.removeAll(deferred);
            succeeded.removeAll(failed);
            outbox.markProcessed(succeeded);
            outbox.markDeferred(deferred, "검색 엔진 사용 불가", DEFER_SECONDS);
            outbox.markFailed(failed, "문서 색인 실패", BACKOFF_SECONDS);
            Set<Long> notApplied = new HashSet<>(result.failedIds());
            notApplied.addAll(result.unavailableIds());
            afterApplied(type, events, notApplied);
        } catch (IOException | ElasticsearchException e) {
            log.warn("검색 엔진 사용 불가로 색인을 미룹니다({}건): {}", eventIds.size(), e.toString());
            outbox.markDeferred(eventIds, e.toString(), DEFER_SECONDS);
        } catch (RuntimeException e) {
            log.warn("검색 색인 동기화 실패({}건): {}", eventIds.size(), e.toString());
            outbox.markFailed(eventIds, e.toString(), BACKOFF_SECONDS);
        }
    }

    private static List<Long> eventsOf(List<Event> events, List<Long> targetIds) {
        return events.stream().filter(event -> targetIds.contains(event.targetId())).map(Event::id).toList();
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
