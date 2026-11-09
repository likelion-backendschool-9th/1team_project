package com.dipsource.service.demo.storage;

import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** R2를 켜지 않은 환경의 자리표시자. 목록·상세는 동작하고, 파일을 다루는 요청만 503을 준다. */
@Component
@ConditionalOnProperty(name = "r2.enabled", havingValue = "false", matchIfMissing = true)
public class UnavailableDemoStorage implements DemoStorage {

    @Override
    public void putFile(String key, Path file, String contentType) {
        throw unavailable();
    }

    @Override
    public void putBytes(String key, byte[] bytes, String contentType) {
        throw unavailable();
    }

    @Override
    public Optional<byte[]> getBytes(String key) {
        throw unavailable();
    }

    @Override
    public void delete(String key) {
        // 파일을 올린 적이 없으므로 지울 것도 없다. 초안 삭제는 R2 없이도 되어야 한다
    }

    @Override
    public void deletePrefix(String prefix) {
        // delete와 같은 이유로 아무것도 하지 않는다
    }

    @Override
    public StorageSummary summarize(String prefix) {
        throw unavailable();
    }

    private static BusinessException unavailable() {
        return new BusinessException(ErrorCode.DEMO_STORAGE_UNAVAILABLE, "데모 저장소가 설정되지 않았습니다");
    }
}
