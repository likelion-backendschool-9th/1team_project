package com.dipsource.service.report;

import com.dipsource.dto.report.ReportResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoStatus;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedComment;
import com.dipsource.entity.feed.FeedStatus;
import com.dipsource.entity.minihome.Guestbook;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostStatus;
import com.dipsource.entity.report.Report;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoRepository;
import com.dipsource.repository.feed.FeedCommentRepository;
import com.dipsource.repository.feed.FeedRepository;
import com.dipsource.repository.minihome.GuestbookRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.report.ReportRepository;
import com.dipsource.repository.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 신고 비즈니스 로직
 *
 * <p>신고 시점의 콘텐츠를 {@code contentSnapshot}으로 DB에 보존
 * 나중에 대상이 삭제되어도 운영진이 내용을 확인 가능
 * 단, 비밀 방명록은 신고자에게 내용이 새지 않도록 스냅샷을 저장하되 응답에는 미포함
 *
 * <p>지원 대상 타입: FEED, POST, DEMO, FEED_COMMENT, GUESTBOOK, USER
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final FeedRepository feedRepository;
    private final PostRepository postRepository;
    private final DemoRepository demoRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final GuestbookRepository guestbookRepository;
    private final UserRepository userRepository;

    // create()가 처리하는 타입 목록 — GUESTBOOK은 createGuestbookReport()로만 처리
    private static final List<String> ALLOWED_TARGET_TYPES =
            List.of("FEED", "POST", "DEMO", "FEED_COMMENT", "USER");

    /**
     * 피드·블로그·데모·피드 댓글·유저 신고 생성
     *
     * @param reporterId 신고자 ID (JWT에서 추출)
     * @param targetType 신고 대상 종류 (FEED / POST / DEMO / FEED_COMMENT / USER)
     * @param targetId   신고 대상 ID
     * @param reason     신고 사유 (카테고리 + 상세 내용)
     * @return 생성된 신고 응답
     * @throws BusinessException 400 — 지원하지 않는 타입, 본인 신고 시도
     * @throws BusinessException 404 — 대상 콘텐츠를 찾을 수 없을 때
     * @throws BusinessException 409 — 같은 대상을 중복 신고할 때
     */
    @Transactional
    public ReportResponse create(Long reporterId, String targetType, Long targetId, String reason) {
        validateTargetType(targetType);
        checkDuplicate(reporterId, targetType, targetId);

        String contentSnapshot = null;
        List<String> imageSnapshotUrls = null;

        switch (targetType) {
            case "FEED" -> {
                Feed feed = validateFeed(targetId, reporterId);
                contentSnapshot = feed.getContent();
                imageSnapshotUrls = feed.getImageUrls();
            }
            case "POST"         -> validatePost(targetId, reporterId);
            case "DEMO"         -> validateDemo(targetId, reporterId);
            case "FEED_COMMENT" -> contentSnapshot = validateFeedComment(targetId, reporterId);
            case "USER"         -> validateUser(targetId, reporterId);
        }

        return saveReport(reporterId, targetType, targetId, reason, contentSnapshot, imageSnapshotUrls);
    }

    /**
     * 방명록 신고 생성
     *
     * <p>방명록은 URL 경로에 미니홈피 주인 ID({@code homeUserId})가 포함되므로,
     * 해당 방명록이 실제로 그 미니홈피에 속하는지 별도로 검증
     * 비밀 방명록은 운영진을 위해 DB에는 스냅샷을 저장하지만, 신고자에게는 내용 미반환
     *
     * @param reporterId   신고자 ID (JWT에서 추출)
     * @param guestbookId  신고 대상 방명록 ID
     * @param homeUserId   URL 경로의 미니홈피 주인 ID — 방명록 소속 검증에 사용
     * @param reason       신고 사유
     * @return 생성된 신고 응답
     * @throws BusinessException 400 — 본인 방명록 신고, 미니홈피 불일치
     * @throws BusinessException 404 — 방명록을 찾을 수 없을 때 (삭제 포함)
     * @throws BusinessException 409 — 같은 방명록을 중복 신고할 때
     */
    @Transactional
    public ReportResponse createGuestbookReport(Long reporterId, Long guestbookId, Long homeUserId, String reason) {
        checkDuplicate(reporterId, "GUESTBOOK", guestbookId);

        Guestbook guestbook = guestbookRepository.findById(guestbookId)
                .filter(g -> g.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 방명록을 찾을 수 없습니다"));

        // URL 경로의 homeUserId와 방명록이 속한 미니홈피의 주인이 일치하는지 확인
        if (!guestbook.getHome().getUserId().equals(homeUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "방명록이 해당 미니홈피에 속하지 않습니다");
        }
        if (guestbook.getWriterId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "본인의 방명록은 신고할 수 없습니다");
        }

        // 비밀글 포함 항상 DB에 저장 — 운영진 확인용. 신고자에게는 ReportResponse에 contentSnapshot 필드가 없어 미노출
        String contentSnapshot = guestbook.getContent();

        return saveReport(reporterId, "GUESTBOOK", guestbookId, reason, contentSnapshot, null);
    }

    // ── private 헬퍼 ──────────────────────────────────────────────────────────

    /**
     * targetType이 허용 목록에 있는지 검증
     * GUESTBOOK은 {@link #createGuestbookReport}로만 처리 — 목록에 미포함
     */
    private void validateTargetType(String targetType) {
        if (!ALLOWED_TARGET_TYPES.contains(targetType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "지원하지 않는 신고 대상 종류입니다");
        }
    }

    /** 같은 (신고자, 대상 타입, 대상 ID) 조합이 이미 존재하면 409 */
    private void checkDuplicate(Long reporterId, String targetType, Long targetId) {
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, targetType, targetId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 신고한 대상입니다");
        }
    }

    /**
     * 피드 존재·활성 상태·본인 여부 확인 후 엔티티 반환
     * 스냅샷(content, imageUrls)은 호출부에서 추출
     */
    private Feed validateFeed(Long feedId, Long reporterId) {
        Feed feed = feedRepository.findById(feedId)
                .filter(f -> f.getStatus() == FeedStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 피드를 찾을 수 없습니다"));
        if (feed.getUserId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "본인의 피드는 신고할 수 없습니다");
        }
        return feed;
    }

    /** 블로그 글 존재·활성 상태·본인 여부 확인 */
    private void validatePost(Long postId, Long reporterId) {
        Post post = postRepository.findById(postId)
                .filter(p -> p.getStatus() == PostStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 블로그 글을 찾을 수 없습니다"));
        if (post.getUserId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "본인의 블로그 글은 신고할 수 없습니다");
        }
    }

    /** 데모 존재·활성 상태·본인 여부 확인 */
    private void validateDemo(Long demoId, Long reporterId) {
        Demo demo = demoRepository.findById(demoId)
                .filter(d -> d.getStatus() == DemoStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 데모를 찾을 수 없습니다"));
        if (demo.getUserId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "본인의 데모는 신고할 수 없습니다");
        }
    }

    /**
     * 피드 댓글 존재·삭제 여부·본인 여부 확인 후 댓글 내용 반환
     *
     * @return 스냅샷으로 저장할 댓글 내용
     */
    private String validateFeedComment(Long commentId, Long reporterId) {
        FeedComment comment = feedCommentRepository.findById(commentId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 댓글을 찾을 수 없습니다"));
        if (comment.getUserId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "본인의 댓글은 신고할 수 없습니다");
        }
        return comment.getContent();
    }

    /** 유저 존재·본인 여부 확인 */
    private void validateUser(Long targetId, Long reporterId) {
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 유저를 찾을 수 없습니다"));
        if (target.getId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "본인을 신고할 수 없습니다");
        }
    }

    /** Report 엔티티 저장 후 응답 DTO 반환 */
    private ReportResponse saveReport(
            Long reporterId, String targetType, Long targetId,
            String reason, String contentSnapshot, List<String> imageSnapshotUrls) {
        Report report = Report.builder()
                .reporterId(reporterId)
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason)
                .contentSnapshot(contentSnapshot)
                .imageSnapshotUrls(imageSnapshotUrls)
                .build();
        reportRepository.save(report);
        return ReportResponse.from(report);
    }
}
