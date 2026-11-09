package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoPreviewResponse;
import com.dipsource.entity.demo.DemoPreview;
import com.dipsource.global.config.DemoProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoPreviewRepository;
import com.dipsource.service.demo.zip.DemoArchive;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * 임시 업로드 기반 미리보기. 파일을 서버로 보내므로 "브라우저 로컬 미리보기"와는 다르다.
 * 정식 업로드와 같은 ZIP 검증을 거치고, 비공개 경로에 저장한 뒤 서명 URL로 제한된 시간만 연다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoPreviewService {

    private final DemoPreviewRepository previewRepository;
    private final DemoArchiveStore archiveStore;
    private final DemoQuotaService quotaService;
    private final DemoUrlSigner urlSigner;
    private final DemoProperties properties;
    private final TransactionTemplate transactionTemplate;

    public DemoPreviewResponse create(Long userId, MultipartFile file) {
        if (previewRepository.countByUserIdAndExpiresAtAfter(userId, LocalDateTime.now())
                >= properties.preview().maxActivePerUser()) {
            throw new BusinessException(ErrorCode.DEMO_QUOTA_EXCEEDED, "임시 미리보기가 너무 많습니다. 잠시 후 다시 시도해 주세요");
        }
        return archiveStore.withValidatedZip(file, archive -> store(userId, archive));
    }

    private DemoPreviewResponse store(Long userId, DemoArchive archive) {
        quotaService.reserveBytes(userId, archive.totalBytes());
        String id = UUID.randomUUID().toString();
        String prefix = prefixOf(id);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(properties.preview().ttlMinutes());
        // 파일보다 행을 먼저 만든다. 서버가 도중에 죽어도 만료 정리가 파일과 예약을 찾을 수 있다
        transactionTemplate.executeWithoutResult(status -> previewRepository.save(DemoPreview.builder()
                .id(id).userId(userId).totalBytes(archive.totalBytes()).expiresAt(expiresAt).build()));
        try {
            archiveStore.putAll(prefix, archive);
        } catch (RuntimeException e) {
            log.error("임시 미리보기 저장 실패: {}", id, e);
            archiveStore.deletePrefixQuietly(prefix + "/");
            transactionTemplate.executeWithoutResult(status -> {
                previewRepository.deleteById(id);
                quotaService.releaseReservedBytes(userId, archive.totalBytes());
            });
            throw new BusinessException(ErrorCode.DEMO_STORAGE_UNAVAILABLE, "미리보기를 만들지 못했습니다");
        }
        transactionTemplate.executeWithoutResult(status -> {
            previewRepository.findById(id).ifPresent(DemoPreview::complete);
            quotaService.commitBytes(userId, archive.totalBytes());
        });
        return new DemoPreviewResponse(
                urlSigner.sign(Map.of("p", prefix), archive.entryPath(), expiresAt.atZone(ZoneId.systemDefault()).toInstant()),
                expiresAt);
    }

    static String prefixOf(String previewId) {
        return "previews/" + previewId;
    }
}
