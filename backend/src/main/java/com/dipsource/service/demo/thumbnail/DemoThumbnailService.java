package com.dipsource.service.demo.thumbnail;

import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoThumbnailStatus;
import com.dipsource.entity.demo.DemoVersion;
import com.dipsource.entity.demo.DemoVersionStatus;
import com.dipsource.global.config.DemoProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoRepository;
import com.dipsource.repository.demo.DemoVersionRepository;
import com.dipsource.service.demo.DemoAccess;
import com.dipsource.service.demo.DemoUrlSigner;
import com.dipsource.service.demo.storage.DemoStorage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * 데모 썸네일.
 *
 * <p>요청 처리와 분리해 DB에 쌓인 대기 버전을 주기적으로 하나씩 찍는다. 서버가 재시작돼도 대기가 사라지지 않고,
 * Browser Run 무료 플랜의 호출 제한(10초에 1회)을 넘지 않는다. 재시도를 다 쓰면 원인을 남기고 기본 썸네일로 둔다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoThumbnailService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_RETRY_MINUTES = 2;
    private static final Duration CAPTURE_URL_TTL = Duration.ofMinutes(5);
    private static final int WIDTH = 640;
    private static final int HEIGHT = 400;
    private static final double QUALITY = 0.8;
    private static final long MAX_MANUAL_BYTES = 2 * 1024 * 1024;
    private static final long MAX_MANUAL_PIXELS = 16_000_000;

    private final DemoVersionRepository versionRepository;
    private final DemoRepository demoRepository;
    private final DemoAccess demoAccess;
    private final DemoStorage storage;
    private final DemoUrlSigner urlSigner;
    private final DemoProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final ObjectProvider<DemoScreenshotClient> screenshotClient;

    @Scheduled(fixedDelayString = "${demo.thumbnail.interval-ms:15000}", initialDelayString = "${demo.thumbnail.interval-ms:15000}")
    public void captureNext() {
        DemoScreenshotClient client = screenshotClient.getIfAvailable();
        if (client == null) {
            return;
        }
        Optional<DemoVersion> due = versionRepository
                .findFirstByStatusAndThumbnailStatusAndThumbnailNextAtLessThanEqualOrderByThumbnailNextAtAsc(
                        DemoVersionStatus.READY, DemoThumbnailStatus.PENDING, LocalDateTime.now());
        due.ifPresent(version -> capture(client, version));
    }

    private void capture(DemoScreenshotClient client, DemoVersion version) {
        if (demoRepository.findById(version.getDemoId()).filter(d -> d.getDeletedAt() == null).isEmpty()) {
            update(version.getId(), DemoVersion::skipThumbnail);
            return;
        }
        String url;
        try {
            String signed = urlSigner.sign(Map.of("p", version.getStoragePrefix()), version.getEntryPath(),
                    Instant.now().plus(CAPTURE_URL_TTL));
            byte[] image = resize(client.capture(signed));
            url = store(version.getDemoId(), image);
        } catch (RuntimeException | IOException e) {
            log.warn("데모 썸네일 생성 실패: versionId={}, attempt={}", version.getId(), version.getThumbnailAttempts() + 1, e);
            update(version.getId(), v -> v.failThumbnail(e.getMessage(), MAX_ATTEMPTS, BASE_RETRY_MINUTES));
            return;
        }
        transactionTemplate.executeWithoutResult(status -> {
            DemoVersion current = versionRepository.findById(version.getId()).orElseThrow();
            current.completeThumbnail(url);
            demoRepository.findById(version.getDemoId())
                    .filter(demo -> version.getId().equals(demo.getCurrentVersionId()) || demo.getThumbnail() == null)
                    .ifPresent(demo -> demo.applyAutoThumbnail(url));
        });
    }

    /** 작성자가 직접 올린 썸네일. 자동 생성 결과로 덮어쓰지 않는다. */
    public String uploadManual(Long userId, Long demoId, MultipartFile file) {
        demoAccess.findOwned(userId, demoId);
        if (file == null || file.isEmpty() || file.getSize() > MAX_MANUAL_BYTES) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "썸네일은 2MB 이하 이미지만 올릴 수 있습니다");
        }
        String url;
        try {
            url = store(demoId, resize(decodeManual(file)));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미지를 읽을 수 없습니다");
        }
        transactionTemplate.executeWithoutResult(status ->
                demoRepository.findById(demoId).ifPresent((Demo demo) -> demo.applyManualThumbnail(url)));
        return url;
    }

    /** 크기를 헤더에서 먼저 확인한다. 작은 파일이 거대한 비트맵으로 풀리는 것을 막는다. */
    private BufferedImage decodeManual(MultipartFile file) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream())) {
            Iterator<ImageReader> readers = input == null ? Collections.emptyIterator() : ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "지원하지 않는 이미지 형식입니다");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                if ((long) reader.getWidth(0) * reader.getHeight(0) > MAX_MANUAL_PIXELS) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "이미지 해상도가 너무 큽니다");
                }
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        }
    }

    private void update(Long versionId, java.util.function.Consumer<DemoVersion> change) {
        transactionTemplate.executeWithoutResult(status -> versionRepository.findById(versionId).ifPresent(change));
    }

    private String store(Long demoId, byte[] image) {
        String name = UUID.randomUUID() + ".jpg";
        storage.putBytes("demos/" + demoId + "/thumbnails/" + name, image, "image/jpeg");
        return properties.workerBaseUrl() + "/thumbnails/" + demoId + "/" + name;
    }

    private byte[] resize(byte[] image) throws IOException {
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(image));
        if (decoded == null) {
            throw new IOException("이미지를 해석하지 못함");
        }
        return resize(decoded);
    }

    private byte[] resize(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Thumbnails.of(image).size(WIDTH, HEIGHT).outputFormat("JPEG").outputQuality(QUALITY).toOutputStream(out);
            return out.toByteArray();
        }
    }
}
