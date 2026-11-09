package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoVersionResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoVersion;
import com.dipsource.global.config.DemoProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoVersionRepository;
import com.dipsource.service.demo.storage.DemoStorage;
import com.dipsource.service.demo.storage.DemoStorageKeys;
import com.dipsource.service.demo.zip.DemoArchive;
import com.dipsource.service.demo.zip.DemoZipValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * 데모 버전 업로드.
 *
 * <p>ZIP 검증이 끝난 파일만 버전 전용 경로로 올린다. 이 경로는 공개 포인터가 가리키기 전까지 Worker로 접근할 수 없다.
 * R2 전송 중에는 DB 트랜잭션을 잡지 않고, 실패하면 올린 파일을 지운 뒤 버전을 FAILED로 남긴다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoVersionService {

    private static final int MAX_PARALLEL_PUTS = 8;

    private final DemoAccess demoAccess;
    private final DemoVersionRepository versionRepository;
    private final DemoZipValidator zipValidator;
    private final DemoStorage storage;
    private final DemoProperties properties;
    private final TransactionTemplate transactionTemplate;

    /** ZIP을 검증해 새 버전으로 저장한다. 공개는 따로 한다. */
    public DemoVersionResponse upload(Long userId, Long demoId, MultipartFile file) {
        Demo demo = demoAccess.findOwned(userId, demoId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "ZIP 파일을 선택해 주세요");
        }
        if (file.getSize() > properties.limits().maxZipBytes()) {
            throw new BusinessException(ErrorCode.PAYLOAD_TOO_LARGE, "ZIP 파일 용량을 초과했습니다");
        }
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("demo-upload-");
            Path zip = workDir.resolve("upload.zip");
            file.transferTo(zip);
            Path extracted = Files.createDirectory(workDir.resolve("files"));
            DemoArchive archive = zipValidator.extract(zip, extracted);
            return store(userId, demoId, archive, demo.getCurrentVersionId());
        } catch (IOException e) {
            log.error("데모 임시 파일 처리 실패: demoId={}", demoId, e);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "업로드한 파일을 처리하지 못했습니다");
        } finally {
            deleteQuietly(workDir);
        }
    }

    private DemoVersionResponse store(Long userId, Long demoId, DemoArchive archive, Long currentVersionId) {
        DemoVersion created = transactionTemplate.execute(status -> createUploading(userId, demoId, archive));
        try {
            putAll(created, archive);
        } catch (RuntimeException e) {
            log.error("데모 파일 저장 실패: demoId={}, versionId={}", demoId, created.getId(), e);
            compensate(created);
            throw new BusinessException(ErrorCode.DEMO_STORAGE_UNAVAILABLE, "파일을 저장하지 못했습니다. 잠시 후 다시 시도해 주세요");
        }
        DemoVersion ready = transactionTemplate.execute(status -> {
            DemoVersion version = versionRepository.findById(created.getId()).orElseThrow();
            version.markReady();
            return version;
        });
        return DemoVersionResponse.of(ready, currentVersionId);
    }

    /** 순번이 겹치지 않게 데모 행을 잠그고 만든다. */
    private DemoVersion createUploading(Long userId, Long demoId, DemoArchive archive) {
        demoAccess.findOwnedForUpdate(userId, demoId);
        DemoVersion version = versionRepository.save(DemoVersion.builder()
                .demoId(demoId)
                .number((int) versionRepository.countByDemoId(demoId) + 1)
                .entryPath(archive.entryPath())
                .fileCount(archive.files().size())
                .totalBytes(archive.totalBytes())
                .build());
        version.assignStoragePrefix(DemoStorageKeys.versionPrefix(demoId, version.getId()));
        return version;
    }

    private void putAll(DemoVersion version, DemoArchive archive) {
        String filesPrefix = DemoStorageKeys.filesPrefix(version.getStoragePrefix());
        Semaphore permits = new Semaphore(MAX_PARALLEL_PUTS);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Void>> uploads = new ArrayList<>();
            for (DemoArchive.File file : archive.files()) {
                uploads.add(executor.submit(() -> {
                    permits.acquire();
                    try {
                        storage.putFile(filesPrefix + file.path(), file.tempFile(), file.contentType());
                    } finally {
                        permits.release();
                    }
                    return null;
                }));
            }
            for (Future<Void> upload : uploads) {
                upload.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    /** 일부만 올라간 파일을 지우고 실패로 표시한다. 여기서 지우지 못해도 정리 작업이 FAILED를 다시 처리한다. */
    private void compensate(DemoVersion version) {
        try {
            storage.deletePrefix(version.getStoragePrefix() + "/");
        } catch (RuntimeException e) {
            log.error("실패한 데모 버전 파일 정리 실패: versionId={}", version.getId(), e);
        }
        transactionTemplate.executeWithoutResult(status ->
                versionRepository.findById(version.getId()).ifPresent(DemoVersion::markFailed));
    }

    private void deleteQuietly(Path dir) {
        if (dir == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        } catch (IOException e) {
            log.warn("데모 임시 폴더 삭제 실패: {}", dir, e);
        }
    }
}
