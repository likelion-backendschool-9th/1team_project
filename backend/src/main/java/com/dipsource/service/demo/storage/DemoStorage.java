package com.dipsource.service.demo.storage;

import java.nio.file.Path;
import java.util.Optional;

/** 데모 정적 파일 저장소. 실제 구현은 R2이고, 테스트는 메모리 구현으로 바꿔 끼운다. */
public interface DemoStorage {

    void putFile(String key, Path file, String contentType);

    void putBytes(String key, byte[] bytes, String contentType);

    Optional<byte[]> getBytes(String key);

    void delete(String key);

    /** 접두사 아래 객체를 모두 지운다. */
    void deletePrefix(String prefix);

    /** 접두사 아래 객체 수와 총 크기. 공개 전에 파일이 온전한지 확인할 때 쓴다. */
    StorageSummary summarize(String prefix);

    record StorageSummary(int fileCount, long totalBytes) {
    }
}
