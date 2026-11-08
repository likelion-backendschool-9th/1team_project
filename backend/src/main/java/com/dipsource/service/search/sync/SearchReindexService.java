package com.dipsource.service.search.sync;

import com.dipsource.dto.search.SearchType;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.search.index.SearchIndexManager;
import com.dipsource.service.search.suggest.SuggestIndexService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 재색인과 정합성 복구. 재색인은 새 인덱스를 다 채운 뒤 Alias를 옮기고, 그동안 쌓인 변경을 다시 적용해서 운영 중에도 안전하다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class SearchReindexService {

    private final SearchIndexManager indexManager;
    private final SuggestIndexService suggestIndex;
    private final SearchOutboxRepository outbox;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "search-reindex");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<String> lastResult = new AtomicReference<>("아직 실행하지 않음");

    /** 전체 재구축을 백그라운드로 시작한다. 이미 돌고 있으면 409 */
    public void startRebuild() {
        if (!running.compareAndSet(false, true)) {
            throw new BusinessException(ErrorCode.CONFLICT, "재색인이 이미 진행 중이에요");
        }
        executor.submit(() -> {
            try {
                lastResult.set(rebuildAll().toString());
            } catch (IOException | RuntimeException e) {
                log.error("검색 재색인 실패", e);
                lastResult.set("실패: " + e);
            } finally {
                running.set(false);
            }
        });
    }

    public boolean isRunning() {
        return running.get();
    }

    public String lastResult() {
        return lastResult.get();
    }

    /** 대상별로 재구축한다. 시작 이후 처리된 변경은 옮겨 간 새 인덱스에 다시 적용한다 */
    public Map<String, Long> rebuildAll() throws IOException {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (SearchType type : SearchIndexManager.TYPES) {
            LocalDateTime started = LocalDateTime.now();
            counts.put(type.name(), indexManager.rebuild(type));
            outbox.replaySince(type, started);
        }
        counts.put("SUGGEST", (long) suggestIndex.rebuild());
        return counts;
    }

    /** 특정 문서를 다시 색인하도록 대기열에 넣는다 */
    public void reindexOne(SearchType type, Long id) {
        outbox.enqueue(type, List.of(id));
    }

    /** 인덱스와 원본을 비교해서 없는 문서, 지워야 할 문서, 내용이 뒤처진 문서를 대기열에 넣는다 */
    public Map<String, Integer> reconcile() throws IOException {
        Map<String, Integer> enqueued = new LinkedHashMap<>();
        for (SearchType type : SearchIndexManager.TYPES) {
            List<Long> differences = indexManager.findDifferences(type);
            if (!differences.isEmpty()) {
                outbox.enqueue(type, differences);
            }
            enqueued.put(type.name(), differences.size());
        }
        return enqueued;
    }

    @Scheduled(cron = "0 40 4 * * *", zone = "Asia/Seoul")
    public void scheduledReconcile() {
        try {
            log.info("검색 정합성 점검: {}", reconcile());
        } catch (IOException | RuntimeException e) {
            log.warn("검색 정합성 점검 실패: {}", e.toString());
        }
    }
}
