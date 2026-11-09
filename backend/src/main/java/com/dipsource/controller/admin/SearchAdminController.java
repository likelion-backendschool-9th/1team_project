package com.dipsource.controller.admin;

import com.dipsource.dto.search.SearchType;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.search.index.SearchIndexManager;
import com.dipsource.service.search.sync.SearchOutboxRepository;
import com.dipsource.service.search.sync.SearchOutboxWorker;
import com.dipsource.service.search.sync.SearchReindexService;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 검색 인덱스 운영. SecurityConfig에서 ADMIN만 허용한다. */
@RestController
@RequestMapping("/admin/search")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class SearchAdminController {

    private final SearchReindexService reindexService;
    private final SearchIndexManager indexManager;
    private final SearchOutboxRepository outbox;

    /** 전체 재색인을 시작한다. 진행 상황은 status로 본다 */
    @PostMapping("/reindex")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void reindex() {
        reindexService.startRebuild();
    }

    /** 문서 하나를 다시 색인한다 */
    @PostMapping("/reindex/{type}/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void reindexOne(@PathVariable String type, @PathVariable Long id) {
        SearchType target = SearchType.from(type);
        if (target == SearchType.ALL) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "대상은 post, demo, feed, user 중 하나예요");
        }
        reindexService.reindexOne(target, id);
    }

    /** 원본과 비교해서 누락·잔존 문서를 복구 대기열에 넣는다 */
    @PostMapping("/reconcile")
    public ApiResponse<Map<String, Integer>> reconcile() throws IOException {
        return ApiResponse.of(reindexService.reconcile());
    }

    /** 재시도 한도를 넘긴 동기화를 다시 시도한다 */
    @PostMapping("/outbox/retry")
    public ApiResponse<Integer> retryDead() {
        return ApiResponse.of(outbox.retryDead(SearchOutboxWorker.MAX_ATTEMPTS));
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() throws IOException {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("reindexRunning", reindexService.isRunning());
        status.put("lastReindex", reindexService.lastResult());
        status.put("outbox", outbox.status(SearchOutboxWorker.MAX_ATTEMPTS));
        Map<String, Long> documents = new LinkedHashMap<>();
        for (SearchType type : SearchIndexManager.TYPES) {
            documents.put(type.name(), indexManager.count(type));
        }
        status.put("documents", documents);
        return ApiResponse.of(status);
    }
}
