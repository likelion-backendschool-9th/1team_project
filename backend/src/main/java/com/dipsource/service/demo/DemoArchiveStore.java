package com.dipsource.service.demo;

import com.dipsource.global.config.DemoProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
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
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** 업로드 ZIP을 임시 폴더에 검증해 풀고 저장소로 보낸다. 임시 파일은 작업이 끝나면 항상 지운다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoArchiveStore {

    private static final int MAX_PARALLEL_PUTS = 8;

    private final DemoZipValidator zipValidator;
    private final DemoStorage storage;
    private final DemoProperties properties;

    /** ZIP을 검증하고 푼 결과로 작업을 실행한다. */
    public <T> T withValidatedZip(MultipartFile file, Function<DemoArchive, T> action) {
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
            return action.apply(zipValidator.extract(zip, extracted));
        } catch (IOException e) {
            log.error("데모 임시 파일 처리 실패", e);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "업로드한 파일을 처리하지 못했습니다");
        } finally {
            deleteQuietly(workDir);
        }
    }

    /** storagePrefix/files/ 아래로 모든 파일을 보낸다. 하나라도 실패하면 예외를 던진다. */
    public void putAll(String storagePrefix, DemoArchive archive) {
        String filesPrefix = DemoStorageKeys.filesPrefix(storagePrefix);
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

    /** 실패한 업로드의 파일을 지운다. 여기서 실패해도 정리 작업이 다시 처리한다. */
    public void deletePrefixQuietly(String prefix) {
        try {
            storage.deletePrefix(prefix);
        } catch (RuntimeException e) {
            log.error("데모 파일 정리 실패: {}", prefix, e);
        }
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
