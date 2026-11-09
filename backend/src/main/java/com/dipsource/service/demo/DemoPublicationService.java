package com.dipsource.service.demo;

import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoStatus;
import com.dipsource.entity.demo.DemoVersion;
import com.dipsource.entity.demo.DemoVersionStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoVersionRepository;
import com.dipsource.service.demo.storage.DemoStorage;
import com.dipsource.service.demo.storage.DemoStorageKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Worker가 읽는 공개 포인터와 DB의 공개 상태를 함께 바꾼다.
 *
 * <p>R2와 DB는 한 트랜잭션으로 묶을 수 없다. DB 행을 잠근 채 커밋 직전에 포인터를 쓰고,
 * 포인터 쓰기가 실패하면 예외로 DB를 되돌린다. 포인터를 쓴 뒤 커밋이 실패하면 이전 포인터로 복원한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoPublicationService {

    private static final String JSON = "application/json";

    private final DemoAccess demoAccess;
    private final DemoVersionRepository versionRepository;
    private final DemoStorage storage;
    private final DemoQuotaService quotaService;
    private final ObjectMapper objectMapper;

    /**
     * 버전을 공개한다. 이전 버전을 고르면 그대로 복구가 된다 — 다시 빌드하지 않고 보관된 파일을 다시 가리킨다.
     *
     * @param versionId         없으면 가장 최근 READY 버전
     * @param expectedCurrentId 있으면 현재 공개 버전과 같을 때만 진행한다. 동시 변경이 뒤늦게 덮어쓰는 것을 막는다
     */
    @Transactional
    public Demo publish(Long userId, Long demoId, Long versionId, Long expectedCurrentId) {
        Demo demo = demoAccess.findOwnedForUpdate(userId, demoId);
        if (demo.getStatus() == DemoStatus.HIDDEN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자가 숨긴 데모는 공개할 수 없습니다");
        }
        if (expectedCurrentId != null && !expectedCurrentId.equals(demo.getCurrentVersionId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "다른 곳에서 공개 버전이 바뀌었습니다. 새로고침 후 다시 시도해 주세요");
        }
        DemoVersion version = findPublishable(demoId, versionId);
        requireFilesIntact(version);
        writePointer(demoId, publicationJson(version, demo.getApiUrl()));
        demo.publish(version);
        if (version.getThumbnailUrl() != null) {
            demo.applyAutoThumbnail(version.getThumbnailUrl());
        }
        return demo;
    }

    /** 외부 백엔드 설정이 바뀌면 공개 중인 데모의 포인터에 바로 반영한다. 공개 전이면 공개할 때 함께 쓰인다. */
    @Transactional
    public void refreshBackend(Demo demo) {
        if (demo.getStatus() != DemoStatus.ACTIVE || demo.getCurrentVersionId() == null) {
            return;
        }
        DemoVersion version = versionRepository.findByIdAndDemoId(demo.getCurrentVersionId(), demo.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT, "공개 중인 버전을 찾을 수 없습니다"));
        writePointer(demo.getId(), publicationJson(version, demo.getApiUrl()));
    }

    /** 공개를 내린다. 파일과 버전은 남기고 포인터만 지운다. */
    @Transactional
    public Demo unpublish(Long userId, Long demoId) {
        Demo demo = demoAccess.findOwnedForUpdate(userId, demoId);
        if (demo.getStatus() == DemoStatus.HIDDEN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자가 숨긴 데모는 변경할 수 없습니다");
        }
        if (demo.getCurrentVersionId() != null) {
            removePointer(demoId);
        }
        demo.unpublish();
        return demo;
    }

    /** 데모를 삭제 상태로 바꾸고 포인터를 지운다. 커밋 시점부터 파일에 접근할 수 없고, 파일 자체는 정리 작업이 지운다. */
    @Transactional
    public void revokeAndDelete(Demo demo) {
        if (demo.getCurrentVersionId() != null) {
            removePointer(demo.getId());
        }
        demo.delete();
        quotaService.releaseDemoSlot(demo.getUserId());
    }

    private DemoVersion findPublishable(Long demoId, Long versionId) {
        Optional<DemoVersion> found = versionId == null
                ? versionRepository.findFirstByDemoIdAndStatusOrderByIdDesc(demoId, DemoVersionStatus.READY)
                : versionRepository.findByIdAndDemoId(versionId, demoId);
        DemoVersion version = found.orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND, "공개할 버전을 찾을 수 없습니다"));
        if (version.getStatus() != DemoVersionStatus.READY) {
            throw new BusinessException(ErrorCode.CONFLICT, "아직 공개할 수 없는 버전입니다");
        }
        return version;
    }

    /** DB에 적힌 파일 수·크기와 실제 저장소를 비교한다. 다르면 공개하지 않아 현재 버전이 그대로 유지된다. */
    private void requireFilesIntact(DemoVersion version) {
        DemoStorage.StorageSummary summary = storage.summarize(DemoStorageKeys.filesPrefix(version.getStoragePrefix()));
        if (summary.fileCount() != version.getFileCount() || summary.totalBytes() != version.getTotalBytes()) {
            log.warn("데모 버전 파일 불일치: versionId={}, expected={}/{}, actual={}/{}", version.getId(),
                    version.getFileCount(), version.getTotalBytes(), summary.fileCount(), summary.totalBytes());
            throw new BusinessException(ErrorCode.CONFLICT, "이 버전의 파일이 온전하지 않아 공개할 수 없습니다");
        }
    }

    private byte[] publicationJson(DemoVersion version, String apiBaseUrl) {
        Map<String, Object> pointer = new LinkedHashMap<>();
        pointer.put("versionId", version.getId());
        pointer.put("storagePrefix", version.getStoragePrefix());
        pointer.put("entryPath", version.getEntryPath());
        if (version.getMockKey() != null) {
            pointer.put("mockKey", version.getMockKey());
        }
        if (apiBaseUrl != null) {
            pointer.put("apiBaseUrl", apiBaseUrl);
        }
        try {
            return objectMapper.writeValueAsBytes(pointer);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private void writePointer(Long demoId, byte[] json) {
        String key = DemoStorageKeys.publication(demoId);
        Optional<byte[]> previous = storageCall(() -> storage.getBytes(key));
        storageCall(() -> {
            storage.putBytes(key, json, JSON);
            return null;
        });
        restoreOnRollback(key, previous);
    }

    private void removePointer(Long demoId) {
        String key = DemoStorageKeys.publication(demoId);
        Optional<byte[]> previous = storageCall(() -> storage.getBytes(key));
        storageCall(() -> {
            storage.delete(key);
            return null;
        });
        restoreOnRollback(key, previous);
    }

    /** 포인터를 바꾼 뒤 커밋이 실패하면 원래 포인터로 되돌린다. */
    private void restoreOnRollback(String key, Optional<byte[]> previous) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    return;
                }
                try {
                    if (previous.isPresent()) {
                        storage.putBytes(key, previous.get(), JSON);
                    } else {
                        storage.delete(key);
                    }
                } catch (RuntimeException e) {
                    log.error("데모 공개 포인터 복원 실패: {}", key, e);
                }
            }
        });
    }

    private <T> T storageCall(java.util.function.Supplier<T> call) {
        try {
            return call.get();
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("데모 저장소 호출 실패", e);
            throw new BusinessException(ErrorCode.DEMO_STORAGE_UNAVAILABLE);
        }
    }
}
