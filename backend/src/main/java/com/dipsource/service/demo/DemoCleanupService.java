package com.dipsource.service.demo;

import com.dipsource.entity.demo.DemoPreview;
import com.dipsource.entity.demo.DemoVersion;
import com.dipsource.entity.demo.DemoVersionStatus;
import com.dipsource.repository.demo.DemoPreviewRepository;
import com.dipsource.repository.demo.DemoRepository;
import com.dipsource.repository.demo.DemoVersionRepository;
import com.dipsource.service.demo.storage.DemoStorage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 더 이상 쓰지 않는 데모 파일을 R2에서 지우고 용량을 돌려준다.
 * 파일 삭제가 성공해야 상태를 바꾸므로 DB와 R2가 어긋나도 다음 주기에 다시 맞춘다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoCleanupService {

    private static final int BATCH_SIZE = 50;
    private static final long STUCK_UPLOAD_HOURS = 1;
    private static final Set<DemoVersionStatus> FILE_USING_STATUSES =
            Set.of(DemoVersionStatus.UPLOADING, DemoVersionStatus.READY, DemoVersionStatus.DELETING);

    private final DemoVersionRepository versionRepository;
    private final DemoPreviewRepository previewRepository;
    private final DemoRepository demoRepository;
    private final DemoQuotaService quotaService;
    private final DemoStorage storage;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelayString = "${demo.cleanup-interval-ms:60000}", initialDelayString = "${demo.cleanup-interval-ms:60000}")
    public void sweep() {
        List<DemoVersion> targets = versionRepository.findPurgeTargets(
                LocalDateTime.now().minusHours(STUCK_UPLOAD_HOURS), PageRequest.of(0, BATCH_SIZE));
        targets.forEach(this::purge);
        previewRepository.findByExpiresAtLessThanEqualOrderByExpiresAtAsc(LocalDateTime.now(), PageRequest.of(0, BATCH_SIZE))
                .forEach(this::purgePreview);
    }

    private void purge(DemoVersion target) {
        try {
            if (target.getMockKey() != null) {
                storage.delete(target.getMockKey());
            }
            if (sharers(target).isEmpty()) {
                storage.deletePrefix(target.getStoragePrefix() + "/");
            }
            demoRepository.findById(target.getDemoId())
                    .filter(demo -> demo.getDeletedAt() != null)
                    .ifPresent(demo -> storage.deletePrefix("demos/" + demo.getId() + "/thumbnails/"));
        } catch (RuntimeException e) {
            log.warn("데모 파일 정리 실패, 다음 주기에 다시 시도: versionId={}", target.getId(), e);
            return;
        }
        transactionTemplate.executeWithoutResult(status ->
                versionRepository.findById(target.getId()).ifPresent(this::releaseAndMarkDeleted));
    }

    /** 상태를 다시 읽어 판단한다. 업로드 중이던 것은 예약을, 저장이 끝난 것은 사용량을 돌려준다. */
    private void releaseAndMarkDeleted(DemoVersion version) {
        Long ownerId = demoRepository.findById(version.getDemoId()).orElseThrow().getUserId();
        DemoVersionStatus before = version.getStatus();
        long released = version.getChargedBytes();
        // 파일을 함께 쓰는 버전이 남아 있으면 파일 용량은 그 버전으로 넘긴다
        List<DemoVersion> sharers = sharers(version);
        if (version.isOwnsFiles() && !sharers.isEmpty()) {
            sharers.get(0).adoptFiles();
            released -= version.getTotalBytes();
        }
        // 용량 UPDATE가 영속성 컨텍스트를 비우므로 상태를 먼저 바꾼다
        version.markDeleted();
        if (before == DemoVersionStatus.UPLOADING) {
            quotaService.releaseReservedBytes(ownerId, released);
        } else if (before == DemoVersionStatus.READY || before == DemoVersionStatus.DELETING) {
            quotaService.releaseUsedBytes(ownerId, released);
        }
    }

    private List<DemoVersion> sharers(DemoVersion version) {
        return versionRepository.findByStoragePrefixAndIdNotAndStatusInOrderByIdAsc(
                version.getStoragePrefix(), version.getId(), FILE_USING_STATUSES);
    }

    private void purgePreview(DemoPreview preview) {
        try {
            storage.deletePrefix(DemoPreviewService.prefixOf(preview.getId()) + "/");
        } catch (RuntimeException e) {
            log.warn("임시 미리보기 정리 실패, 다음 주기에 다시 시도: {}", preview.getId(), e);
            return;
        }
        transactionTemplate.executeWithoutResult(status -> {
            if (preview.isCompleted()) {
                quotaService.releaseUsedBytes(preview.getUserId(), preview.getTotalBytes());
            } else {
                quotaService.releaseReservedBytes(preview.getUserId(), preview.getTotalBytes());
            }
            previewRepository.deleteById(preview.getId());
        });
    }
}
