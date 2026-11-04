package com.dipsource.service.report;

import com.dipsource.dto.report.ReportCreateRequest;
import com.dipsource.dto.report.ReportResponse;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedStatus;
import com.dipsource.entity.report.Report;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.feed.FeedRepository;
import com.dipsource.repository.report.ReportRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 신고 비즈니스 로직
 * 신고 시점에 대상 콘텐츠를 스냅샷으로 복사해 보존함
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final FeedRepository feedRepository;

    /**
     * 신고 생성
     * 대상이 FEED면 피드 내용을 스냅샷으로 저장함
     * 같은 대상을 중복 신고하면 409 반환
     *
     * @param userId  신고자 ID (JWT에서 추출)
     * @param request 신고 요청 (targetType, targetId, reason)
     * @return 생성된 신고 응답
     */
    private static final List<String> ALLOWED_TARGET_TYPES = List.of("FEED", "POST", "DEMO", "FEED_COMMENT", "GUESTBOOK", "USER");

    @Transactional
    public ReportResponse create(Long userId, ReportCreateRequest request) {
        // targetType 검증
        if (!ALLOWED_TARGET_TYPES.contains(request.targetType())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "지원하지 않는 신고 대상 종류입니다");
        }

        // 중복 신고 확인
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                userId, request.targetType(), request.targetId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 신고한 대상입니다");
        }

        // 스냅샷 추출
        String contentSnapshot = null;
        List<String> imageSnapshotUrls = null;

        if ("FEED".equals(request.targetType())) {
            Feed feed = feedRepository.findById(request.targetId())
                    .filter(f -> f.getStatus() == FeedStatus.ACTIVE)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 피드를 찾을 수 없습니다"));
            if (feed.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "본인의 피드는 신고할 수 없습니다");
            }
            contentSnapshot = feed.getContent();
            imageSnapshotUrls = feed.getImageUrls();
        }
        // TODO: POST, FEED_COMMENT 등 다른 타입은 추후 추가

        Report report = Report.builder()
                .reporterId(userId)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .contentSnapshot(contentSnapshot)
                .imageSnapshotUrls(imageSnapshotUrls)
                .build();
        reportRepository.save(report);

        return ReportResponse.from(report);
    }
}
