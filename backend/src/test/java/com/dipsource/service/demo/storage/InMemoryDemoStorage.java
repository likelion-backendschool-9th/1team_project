package com.dipsource.service.demo.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/** R2 대신 쓰는 메모리 저장소. 지정한 키에서 실패하도록 만들어 보상 처리를 검증한다. */
public class InMemoryDemoStorage implements DemoStorage {

    private final Map<String, byte[]> objects = new ConcurrentHashMap<>();
    private final AtomicInteger puts = new AtomicInteger();
    private volatile Predicate<String> failingPuts = key -> false;
    private volatile Predicate<String> failingDeletes = key -> false;

    public void reset() {
        objects.clear();
        puts.set(0);
        failingPuts = key -> false;
        failingDeletes = key -> false;
    }

    public void failPutsWhen(Predicate<String> condition) {
        failingPuts = condition;
    }

    public void failDeletesWhen(Predicate<String> condition) {
        failingDeletes = condition;
    }

    public boolean has(String key) {
        return objects.containsKey(key);
    }

    public byte[] bytes(String key) {
        return objects.get(key);
    }

    public long countUnder(String prefix) {
        return objects.keySet().stream().filter(key -> key.startsWith(prefix)).count();
    }

    public int putCount() {
        return puts.get();
    }

    /** 저장소에서 파일이 사라진 상황을 흉내 낸다 */
    public void remove(String key) {
        objects.remove(key);
    }

    @Override
    public void putFile(String key, Path file, String contentType) {
        try {
            putBytes(key, Files.readAllBytes(file), contentType);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void putBytes(String key, byte[] bytes, String contentType) {
        puts.incrementAndGet();
        if (failingPuts.test(key)) {
            throw new IllegalStateException("저장 실패: " + key);
        }
        objects.put(key, bytes);
    }

    @Override
    public Optional<byte[]> getBytes(String key) {
        return Optional.ofNullable(objects.get(key));
    }

    @Override
    public void delete(String key) {
        if (failingDeletes.test(key)) {
            throw new IllegalStateException("삭제 실패: " + key);
        }
        objects.remove(key);
    }

    @Override
    public void deletePrefix(String prefix) {
        if (failingDeletes.test(prefix)) {
            throw new IllegalStateException("삭제 실패: " + prefix);
        }
        objects.keySet().removeIf(key -> key.startsWith(prefix));
    }

    @Override
    public StorageSummary summarize(String prefix) {
        return new StorageSummary(
                (int) countUnder(prefix),
                objects.entrySet().stream().filter(e -> e.getKey().startsWith(prefix)).mapToLong(e -> e.getValue().length).sum());
    }
}
