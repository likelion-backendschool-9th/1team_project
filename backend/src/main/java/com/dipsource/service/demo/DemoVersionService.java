package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoEditorSaveRequest;
import com.dipsource.dto.demo.DemoEditorSource;
import com.dipsource.dto.demo.DemoPreviewResponse;
import com.dipsource.dto.demo.DemoVersionConfigRequest;
import com.dipsource.dto.demo.DemoVersionConfigResponse;
import com.dipsource.dto.demo.DemoVersionResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoVersion;
import com.dipsource.entity.demo.DemoVersionStatus;
import com.dipsource.global.config.DemoProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoVersionRepository;
import com.dipsource.service.demo.mock.CompiledMock;
import com.dipsource.service.demo.mock.DemoMockCompiler;
import com.dipsource.service.demo.storage.DemoStorage;
import com.dipsource.service.demo.storage.DemoStorageKeys;
import com.dipsource.service.demo.thumbnail.DemoScreenshotClient;
import com.dipsource.service.demo.zip.DemoArchive;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
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
    private final DemoMockCompiler mockCompiler;
    private final DemoEditorArchiveBuilder editorArchiveBuilder;
    private final DemoStorage storage;
    private final ObjectMapper objectMapper;

    /** ZIP을 검증해 새 버전으로 저장한다. 공개는 따로 한다. */
    public DemoVersionResponse upload(Long userId, Long demoId, MultipartFile file) {
        Demo demo = demoAccess.findOwned(userId, demoId);
        return archiveStore.withValidatedZip(file,
                archive -> store(userId, demoId, archive, demo.getCurrentVersionId(), DemoMockCompiler.EMPTY, null));
    }

    /** 코드 편집기 내용을 정적 파일로 만들어 ZIP과 같은 경로로 저장한다. */
    public DemoVersionResponse saveEditor(Long userId, Long demoId, DemoEditorSaveRequest request) {
        Demo demo = demoAccess.findOwned(userId, demoId);
        CompiledMock mock = mockCompiler.compile(request.mockConfig(), request.sampleData());
        String source = editorSourceJson(request);
        return editorArchiveBuilder.withArchive(request,
                archive -> store(userId, demoId, archive, demo.getCurrentVersionId(), mock, source));
    }

    /** 버전의 Mock·샘플 데이터와 편집기 코드. */
    @Transactional(readOnly = true)
    public DemoVersionConfigResponse getConfig(Long userId, Long demoId, Long versionId) {
        demoAccess.findOwned(userId, demoId);
        DemoVersion version = findReady(demoId, versionId);
        return new DemoVersionConfigResponse(version.getMockConfig(), version.getSampleData(), parseEditorSource(version));
    }

    /**
     * Mock·샘플 데이터를 바꾼 새 버전을 만든다. 공개 중인 버전을 직접 고치지 않으므로 복구하면 이전 설정도 함께 돌아온다.
     * 정적 파일은 다시 올리지 않고 원본 버전과 함께 쓴다.
     */
    public DemoVersionResponse saveConfig(Long userId, Long demoId, Long versionId, DemoVersionConfigRequest request) {
        Demo owned = demoAccess.findOwned(userId, demoId);
        CompiledMock mock = mockCompiler.compile(request.mockConfig(), request.sampleData());
        long bytes = mock.json() == null ? 0 : mock.json().length;

        quotaService.reserveBytes(userId, bytes);
        DemoVersion created;
        try {
            created = transactionTemplate.execute(status -> createDerived(userId, demoId, versionId, mock, bytes));
        } catch (RuntimeException e) {
            quotaService.releaseReservedBytes(userId, bytes);
            throw e;
        }
        try {
            putMock(created, mock);
        } catch (RuntimeException e) {
            log.error("Mock 설정 저장 실패: demoId={}, versionId={}", demoId, created.getId(), e);
            transactionTemplate.executeWithoutResult(status -> {
                versionRepository.findById(created.getId()).ifPresent(DemoVersion::markFailed);
                quotaService.releaseReservedBytes(userId, bytes);
            });
            throw new BusinessException(ErrorCode.DEMO_STORAGE_UNAVAILABLE, "설정을 저장하지 못했습니다. 잠시 후 다시 시도해 주세요");
        }
        return DemoVersionResponse.of(finishReady(userId, created.getId(), bytes), owned.getCurrentVersionId());
    }

    private DemoVersion createDerived(Long userId, Long demoId, Long versionId, CompiledMock mock, long bytes) {
        demoAccess.findOwnedForUpdate(userId, demoId);
        requireVersionSlot(demoId);
        DemoVersion source = findReady(demoId, versionId);
        DemoVersion version = versionRepository.save(DemoVersion.builder()
                .demoId(demoId)
                .number((int) versionRepository.countByDemoId(demoId) + 1)
                .entryPath(source.getEntryPath())
                .fileCount(source.getFileCount())
                .totalBytes(source.getTotalBytes())
                .chargedBytes(bytes)
                .ownsFiles(false)
                .mockConfig(mock.mockConfig())
                .sampleData(mock.sampleData())
                .editorSource(source.getEditorSource())
                .build());
        version.assignStoragePrefix(source.getStoragePrefix());
        if (mock.json() != null) {
            version.assignMockKey(mockKeyOf(demoId, version.getId()));
        }
        return version;
    }

    private DemoVersion findReady(Long demoId, Long versionId) {
        return versionRepository.findByIdAndDemoId(versionId, demoId)
                .filter(v -> v.getStatus() == DemoVersionStatus.READY)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "버전을 찾을 수 없습니다"));
    }

    private void putMock(DemoVersion version, CompiledMock mock) {
        if (mock.json() != null) {
            storage.putBytes(version.getMockKey(), mock.json(), "application/json");
        }
    }

    private String mockKeyOf(Long demoId, Long versionId) {
        return DemoStorageKeys.versionPrefix(demoId, versionId) + "/mock.json";
    }

    private String editorSourceJson(DemoEditorSaveRequest request) {
        try {
            return objectMapper.writeValueAsString(new DemoEditorSource(request.html(), request.css(), request.js()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private DemoEditorSource parseEditorSource(DemoVersion version) {
        if (version.getEditorSource() == null) {
            return null;
        }
        try {
            return objectMapper.readValue(version.getEditorSource(), DemoEditorSource.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
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
        Demo demo = demoAccess.findOwned(userId, demoId);
        DemoVersion version = findReady(demoId, versionId);
        Instant expiresAt = Instant.now().plus(VERSION_PREVIEW_TTL);
        return new DemoPreviewResponse(
                urlSigner.sign(urlSigner.grantOf(version, demo.getApiUrl()), version.getEntryPath(), expiresAt),
                LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()));
    }

    private DemoVersionResponse store(Long userId, Long demoId, DemoArchive archive, Long currentVersionId,
                                      CompiledMock mock, String editorSource) {
        long bytes = archive.totalBytes() + (mock.json() == null ? 0 : mock.json().length);
        // 데모 행을 잠그기 전에 예약한다. 한도 초과면 버전 행이 생기지 않는다
        quotaService.reserveBytes(userId, bytes);
        DemoVersion created;
        try {
            created = transactionTemplate.execute(status -> createUploading(userId, demoId, archive, bytes, mock, editorSource));
        } catch (RuntimeException e) {
            quotaService.releaseReservedBytes(userId, bytes);
            throw e;
        }
        try {
            archiveStore.putAll(created.getStoragePrefix(), archive);
            putMock(created, mock);
        } catch (RuntimeException e) {
            log.error("데모 파일 저장 실패: demoId={}, versionId={}", demoId, created.getId(), e);
            compensate(userId, created);
            throw new BusinessException(ErrorCode.DEMO_STORAGE_UNAVAILABLE, "파일을 저장하지 못했습니다. 잠시 후 다시 시도해 주세요");
        }
        return DemoVersionResponse.of(finishReady(userId, created.getId(), bytes), currentVersionId);
    }

    private DemoVersion finishReady(Long userId, Long versionId, long bytes) {
        return transactionTemplate.execute(status -> {
            DemoVersion version = versionRepository.findById(versionId).orElseThrow();
            version.markReady();
            if (screenshotClient.getIfAvailable() != null) {
                version.scheduleThumbnail();
            }
            quotaService.commitBytes(userId, bytes);
            return version;
        });
    }

    /** 순번이 겹치지 않게 데모 행을 잠그고 만든다. */
    private DemoVersion createUploading(Long userId, Long demoId, DemoArchive archive, long bytes,
                                        CompiledMock mock, String editorSource) {
        demoAccess.findOwnedForUpdate(userId, demoId);
        requireVersionSlot(demoId);
        DemoVersion version = versionRepository.save(DemoVersion.builder()
                .demoId(demoId)
                .number((int) versionRepository.countByDemoId(demoId) + 1)
                .entryPath(archive.entryPath())
                .fileCount(archive.files().size())
                .totalBytes(archive.totalBytes())
                .chargedBytes(bytes)
                .mockConfig(mock.mockConfig())
                .sampleData(mock.sampleData())
                .editorSource(editorSource)
                .build());
        version.assignStoragePrefix(DemoStorageKeys.versionPrefix(demoId, version.getId()));
        if (mock.json() != null) {
            version.assignMockKey(mockKeyOf(demoId, version.getId()));
        }
        return version;
    }

    private void requireVersionSlot(Long demoId) {
        int maxVersions = properties.quota().maxVersionsPerDemo();
        if (versionRepository.countByDemoIdAndStatusIn(demoId, LIVE_STATUSES) >= maxVersions) {
            throw new BusinessException(ErrorCode.DEMO_QUOTA_EXCEEDED,
                    "버전은 " + maxVersions + "개까지 보관할 수 있습니다. 이전 버전을 지워 주세요");
        }
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
