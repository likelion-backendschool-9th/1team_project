package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoPreviewResponse;
import com.dipsource.dto.demo.DemoVersionResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoVersion;
import com.dipsource.entity.demo.DemoVersionStatus;
import com.dipsource.global.config.DemoProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoVersionRepository;
import com.dipsource.service.demo.storage.DemoStorageKeys;
import com.dipsource.service.demo.thumbnail.DemoScreenshotClient;
import com.dipsource.service.demo.zip.DemoArchive;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * 데모 버전 업로드·조회·삭제.
 *
 * <p>ZIP 검증이 끝난 파일만 버전 전용 경로로 올린다. 이 경로는 공개 포인터가 가리키기 전까지 Worker로 접근할 수 없다.
 * R2 전송 중에는 DB 트랜잭션을 잡지 않고, 실패하면 올린 파일을 지운 뒤 버전을 FAILED로 남긴다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoVersionService {

    private static final Set<DemoVersionStatus> LIVE_STATUSES =
            Set.of(DemoVersionStatus.UPLOADING, DemoVersionStatus.READY);
    private static final Set<DemoVersionStatus> VISIBLE_STATUSES = Set.of(DemoVersionStatus.READY);
    private static final Duration VERSION_PREVIEW_TTL = Duration.ofMinutes(10);

    private final DemoAccess demoAccess;
    private final DemoVersionRepository versionRepository;
    private final DemoArchiveStore archiveStore;
    private final DemoQuotaService quotaService;
    private final DemoUrlSigner urlSigner;
    private final DemoProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final ObjectProvider<DemoScreenshotClient> screenshotClient;

    /** ZIP을 검증해 새 버전으로 저장한다. 공개는 따로 한다. */
    public DemoVersionResponse upload(Long userId, Long demoId, MultipartFile file) {
        Demo demo = demoAccess.findOwned(userId, demoId);
        return archiveStore.withValidatedZip(file, archive -> store(userId, demoId, archive, demo.getCurrentVersionId()));
    }

    @Transactional(readOnly = true)
    public List<DemoVersionResponse> list(Long userId, Long demoId) {
        Demo demo = demoAccess.findOwned(userId, demoId);
        return versionRepository.findByDemoIdAndStatusInOrderByIdDesc(demoId, VISIBLE_STATUSES).stream()
                .map(version -> DemoVersionResponse.of(version, demo.getCurrentVersionId()))
                .toList();
    }

    /** 보관 중인 버전을 지운다. 공개 중인 버전은 지울 수 없다. */
    @Transactional
    public void delete(Long userId, Long demoId, Long versionId) {
        Demo demo = demoAccess.findOwnedForUpdate(userId, demoId);
        DemoVersion version = versionRepository.findByIdAndDemoId(versionId, demoId)
                .filter(v -> v.getStatus() == DemoVersionStatus.READY)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "버전을 찾을 수 없습니다"));
        if (version.getId().equals(demo.getCurrentVersionId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "공개 중인 버전은 지울 수 없습니다");
        }
        version.markDeleting();
    }

    /** 공개 전 버전을 작성자가 확인할 임시 URL. */
    @Transactional(readOnly = true)
    public DemoPreviewResponse previewUrl(Long userId, Long demoId, Long versionId) {
        demoAccess.findOwned(userId, demoId);
        DemoVersion version = versionRepository.findByIdAndDemoId(versionId, demoId)
                .filter(v -> v.getStatus() == DemoVersionStatus.READY)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "버전을 찾을 수 없습니다"));
        Instant expiresAt = Instant.now().plus(VERSION_PREVIEW_TTL);
        return new DemoPreviewResponse(
                urlSigner.sign(Map.of("p", version.getStoragePrefix()), version.getEntryPath(), expiresAt),
                LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()));
    }

    private DemoVersionResponse store(Long userId, Long demoId, DemoArchive archive, Long currentVersionId) {
        // 데모 행을 잠그기 전에 예약한다. 한도 초과면 버전 행이 생기지 않는다
        quotaService.reserveBytes(userId, archive.totalBytes());
        DemoVersion created;
        try {
            created = transactionTemplate.execute(status -> createUploading(userId, demoId, archive));
        } catch (RuntimeException e) {
            quotaService.releaseReservedBytes(userId, archive.totalBytes());
            throw e;
        }
        try {
            archiveStore.putAll(created.getStoragePrefix(), archive);
        } catch (RuntimeException e) {
            log.error("데모 파일 저장 실패: demoId={}, versionId={}", demoId, created.getId(), e);
            compensate(userId, created);
            throw new BusinessException(ErrorCode.DEMO_STORAGE_UNAVAILABLE, "파일을 저장하지 못했습니다. 잠시 후 다시 시도해 주세요");
        }
        DemoVersion ready = transactionTemplate.execute(status -> {
            DemoVersion version = versionRepository.findById(created.getId()).orElseThrow();
            version.markReady();
            if (screenshotClient.getIfAvailable() != null) {
                version.scheduleThumbnail();
            }
            quotaService.commitBytes(userId, archive.totalBytes());
            return version;
        });
        return DemoVersionResponse.of(ready, currentVersionId);
    }

    /** 순번이 겹치지 않게 데모 행을 잠그고 만든다. */
    private DemoVersion createUploading(Long userId, Long demoId, DemoArchive archive) {
        demoAccess.findOwnedForUpdate(userId, demoId);
        int maxVersions = properties.quota().maxVersionsPerDemo();
        if (versionRepository.countByDemoIdAndStatusIn(demoId, LIVE_STATUSES) >= maxVersions) {
            throw new BusinessException(ErrorCode.DEMO_QUOTA_EXCEEDED,
                    "버전은 " + maxVersions + "개까지 보관할 수 있습니다. 이전 버전을 지워 주세요");
        }
        DemoVersion version = versionRepository.save(DemoVersion.builder()
                .demoId(demoId)
                .number((int) versionRepository.countByDemoId(demoId) + 1)
                .entryPath(archive.entryPath())
                .fileCount(archive.files().size())
                .totalBytes(archive.totalBytes())
                .chargedBytes(archive.totalBytes())
                .build());
        version.assignStoragePrefix(DemoStorageKeys.versionPrefix(demoId, version.getId()));
        return version;
    }

    /** 일부만 올라간 파일을 지우고 예약을 돌려준 뒤 실패로 표시한다. */
    private void compensate(Long userId, DemoVersion version) {
        archiveStore.deletePrefixQuietly(version.getStoragePrefix() + "/");
        transactionTemplate.executeWithoutResult(status -> {
            versionRepository.findById(version.getId()).ifPresent(DemoVersion::markFailed);
            quotaService.releaseReservedBytes(userId, version.getChargedBytes());
        });
    }
}
