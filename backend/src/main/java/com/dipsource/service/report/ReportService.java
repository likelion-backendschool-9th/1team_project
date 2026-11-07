package com.dipsource.service.report;

import com.dipsource.dto.report.ReportResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoStatus;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedStatus;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostStatus;
import com.dipsource.entity.report.Report;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.entity.feed.FeedComment;
import com.dipsource.entity.minihome.Guestbook;
import com.dipsource.entity.user.User;
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
 * 신고 시점에 대상 콘텐츠를 스냅샷으로 복사해 보존함
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
    public ReportResponse create(Long userId, String targetType, Long targetId, String reason) {
        // targetType 검증
        if (!ALLOWED_TARGET_TYPES.contains(targetType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "지원하지 않는 신고 대상 종류입니다");
        }

        // 중복 신고 확인
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                userId, targetType, targetId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 신고한 대상입니다");
        }

        // 스냅샷 추출
        String contentSnapshot = null;
        List<String> imageSnapshotUrls = null;

        if ("FEED".equals(targetType)) {
            Feed feed = feedRepository.findById(targetId)
                    .filter(f -> f.getStatus() == FeedStatus.ACTIVE)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 피드를 찾을 수 없습니다"));
            if (feed.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "본인의 피드는 신고할 수 없습니다");
            }
            contentSnapshot = feed.getContent();
            imageSnapshotUrls = feed.getImageUrls();
        } else if ("POST".equals(targetType)) {
            Post post = postRepository.findById(targetId)
                    .filter(p -> p.getStatus() == PostStatus.ACTIVE)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 블로그 글을 찾을 수 없습니다"));
            if (post.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "본인의 블로그 글은 신고할 수 없습니다");
            }
        } else if ("DEMO".equals(targetType)) {
            Demo demo = demoRepository.findById(targetId)
                    .filter(d -> d.getStatus() == DemoStatus.ACTIVE)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 데모를 찾을 수 없습니다"));
            if (demo.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "본인의 데모는 신고할 수 없습니다");
            }
        } else if ("FEED_COMMENT".equals(targetType)) {
            FeedComment comment = feedCommentRepository.findById(targetId)
                    .filter(c -> c.getDeletedAt() == null)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 댓글을 찾을 수 없습니다"));
            if (comment.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "본인의 댓글은 신고할 수 없습니다");
            }
            contentSnapshot = comment.getContent();
        } else if ("GUESTBOOK".equals(targetType)) {
            Guestbook guestbook = guestbookRepository.findById(targetId)
                    .filter(g -> g.getDeletedAt() == null)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 방명록을 찾을 수 없습니다"));
            if (guestbook.getWriterId().equals(userId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "본인의 방명록은 신고할 수 없습니다");
            }
            contentSnapshot = guestbook.getContent();
        } else if ("USER".equals(targetType)) {
            User target = userRepository.findById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 유저를 찾을 수 없습니다"));
            if (target.getId().equals(userId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "본인을 신고할 수 없습니다");
            }
        }

        Report report = Report.builder()
                .reporterId(userId)
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
