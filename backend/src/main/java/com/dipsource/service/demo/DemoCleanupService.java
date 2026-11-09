package com.dipsource.service.demo;

import com.dipsource.entity.demo.DemoVersion;
import com.dipsource.repository.demo.DemoVersionRepository;
import com.dipsource.service.demo.storage.DemoStorage;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/** 더 이상 쓰지 않는 데모 파일을 R2에서 지운다. DB와 R2가 어긋나도 다음 주기에 다시 맞춘다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoCleanupService {

    private static final int BATCH_SIZE = 50;
    private static final long STUCK_UPLOAD_HOURS = 1;

    private final DemoVersionRepository versionRepository;
    private final DemoStorage storage;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelayString = "${demo.cleanup-interval-ms:60000}", initialDelayString = "${demo.cleanup-interval-ms:60000}")
    public void sweep() {
        List<DemoVersion> targets = versionRepository.findPurgeTargets(
                LocalDateTime.now().minusHours(STUCK_UPLOAD_HOURS), PageRequest.of(0, BATCH_SIZE));
        for (DemoVersion target : targets) {
            purge(target);
        }
    }

    private void purge(DemoVersion target) {
        try {
            storage.deletePrefix(target.getStoragePrefix() + "/");
        } catch (RuntimeException e) {
            log.warn("데모 파일 정리 실패, 다음 주기에 다시 시도: versionId={}", target.getId(), e);
            return;
        }
        transactionTemplate.executeWithoutResult(status ->
                versionRepository.findById(target.getId()).ifPresent(DemoVersion::markDeleted));
    }
}
