package com.dipsource.service.admin.impl;

import com.dipsource.dto.admin.AdminReportResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedComment;
import com.dipsource.entity.minihome.Guestbook;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.report.Report;
import com.dipsource.entity.report.ReportStatus;
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
import com.dipsource.service.admin.AdminReportService;
import com.dipsource.global.common.PageResponse;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Map<String, String> TYPE_QUERY = Map.of(
            "feeds", "FEED",
            "posts", "POST",
            "demos", "DEMO",
            "comments", "FEED_COMMENT",
            "guestbooks", "GUESTBOOK",
            "users", "USER");

    private static final Map<String, String> TYPE_LABEL = Map.of(
            "FEED", "피드",
            "POST", "블로그",
            "DEMO", "데모",
            "FEED_COMMENT", "댓글",
            "GUESTBOOK", "방명록",
            "USER", "회원");

    private static final Map<String, ReportStatus> STATUS_QUERY = Map.of(
            "pending", ReportStatus.PENDING,
            "resolved", ReportStatus.RESOLVED,
            "rejected", ReportStatus.REJECTED);

    private static final Set<String> RESOLVE_DECISIONS = Set.of("hide", "sanction");

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final PostRepository postRepository;
    private final DemoRepository demoRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final GuestbookRepository guestbookRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminReportResponse> list(String type, String status, Pageable pageable) {
        int size = pageable.getPageSize() <= 0 ? 10 : Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable limited = PageRequest.of(pageable.getPageNumber(), size);
        Page<Report> page = reportRepository.searchForAdmin(parseStatus(status), parseType(type), limited);
        return PageResponse.of(page, mapAll(page.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminReportResponse get(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다"));
        return mapAll(List.of(report)).getFirst();
    }

    @Override
    @Transactional
    public void handle(Long adminId, Long reportId, String decision, String memo) {
        Report report = reportRepository.findByIdForUpdate(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다"));
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 처리된 신고입니다");
        }
        String note = memo == null ? "" : memo.strip();
        if (RESOLVE_DECISIONS.contains(decision)) {
            report.resolve(adminId, note);
            return;
        }
        if ("reject".equals(decision)) {
            report.reject(adminId, note);
            return;
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "지원하지 않는 처리 방식입니다");
    }

    private String parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        String mapped = TYPE_QUERY.get(type.strip().toLowerCase(Locale.ROOT));
        if (mapped == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "지원하지 않는 신고 유형입니다");
        }
        return mapped;
    }

    private ReportStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        ReportStatus mapped = STATUS_QUERY.get(status.strip().toLowerCase(Locale.ROOT));
        if (mapped == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "지원하지 않는 신고 상태입니다");
        }
        return mapped;
    }

    private List<AdminReportResponse> mapAll(List<Report> reports) {
        if (reports.isEmpty()) {
            return List.of();
        }
        Map<Long, Feed> feeds = load(reports, "FEED", feedRepository::findAllById, Feed::getId);
        Map<Long, Post> posts = load(reports, "POST", postRepository::findAllById, Post::getId);
        Map<Long, Demo> demos = load(reports, "DEMO", demoRepository::findAllById, Demo::getId);
        Map<Long, FeedComment> comments = load(
                reports, "FEED_COMMENT", feedCommentRepository::findAllWithFeedByIdIn, FeedComment::getId);
        Map<Long, Guestbook> guestbooks = load(
                reports, "GUESTBOOK", guestbookRepository::findAllWithHomeByIdIn, Guestbook::getId);

        Set<Long> userIds = new HashSet<>();
        reports.forEach(report -> userIds.add(report.getReporterId()));
        reports.stream().map(Report::getHandledBy).filter(Objects::nonNull).forEach(userIds::add);
        feeds.values().forEach(feed -> userIds.add(feed.getUserId()));
        posts.values().forEach(post -> userIds.add(post.getUserId()));
        demos.values().forEach(demo -> userIds.add(demo.getUserId()));
        comments.values().forEach(comment -> userIds.add(comment.getUserId()));
        guestbooks.values().forEach(guestbook -> userIds.add(guestbook.getWriterId()));
        reports.stream()
                .filter(report -> "USER".equals(report.getTargetType()))
                .forEach(report -> userIds.add(report.getTargetId()));

        Map<Long, User> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return reports.stream()
                .map(report -> toResponse(report, users, feeds, posts, demos, comments, guestbooks))
                .toList();
    }

    private AdminReportResponse toResponse(
            Report report,
            Map<Long, User> users,
            Map<Long, Feed> feeds,
            Map<Long, Post> posts,
            Map<Long, Demo> demos,
            Map<Long, FeedComment> comments,
            Map<Long, Guestbook> guestbooks) {
        String reporter = nickname(users, report.getReporterId());
        String reported = "삭제된 유저";
        String title = report.getContentSnapshot();
        String href = "";
        switch (report.getTargetType()) {
            case "FEED" -> {
                Feed feed = feeds.get(report.getTargetId());
                if (feed != null) {
                    reported = nickname(users, feed.getUserId());
                    if (title == null || title.isBlank()) {
                        title = feed.getContent();
                    }
                }
                href = "/feeds/" + report.getTargetId();
            }
            case "POST" -> {
                Post post = posts.get(report.getTargetId());
                if (post != null) {
                    reported = nickname(users, post.getUserId());
                    title = post.getTitle();
                }
                href = "/blog/posts/" + report.getTargetId();
            }
            case "DEMO" -> {
                Demo demo = demos.get(report.getTargetId());
                if (demo != null) {
                    reported = nickname(users, demo.getUserId());
                    title = demo.getTitle();
                }
                href = "/demos/" + report.getTargetId();
            }
            case "FEED_COMMENT" -> {
                FeedComment comment = comments.get(report.getTargetId());
                if (comment != null) {
                    reported = nickname(users, comment.getUserId());
                    if (title == null || title.isBlank()) {
                        title = comment.getContent();
                    }
                    href = "/feeds/" + comment.getFeed().getId();
                }
            }
            case "GUESTBOOK" -> {
                Guestbook guestbook = guestbooks.get(report.getTargetId());
                if (guestbook != null) {
                    reported = nickname(users, guestbook.getWriterId());
                    if (title == null || title.isBlank()) {
                        title = guestbook.getContent();
                    }
                    href = "/mini-home/" + guestbook.getHome().getUserId() + "/guestbook";
                }
            }
            case "USER" -> {
                reported = nickname(users, report.getTargetId());
                title = reported;
                href = "/admin/users/" + report.getTargetId();
            }
            default -> {
            }
        }
        return AdminReportResponse.of(
                report.getId(),
                TYPE_LABEL.getOrDefault(report.getTargetType(), report.getTargetType()),
                title,
                href,
                report.getReason(),
                report.getContentSnapshot(),
                reporter,
                reported,
                report.getCreatedAt(),
                report.getStatus().name().toLowerCase(Locale.ROOT),
                report.getHandledBy() == null ? null : nickname(users, report.getHandledBy()),
                report.getAdminNote(),
                report.getHandledAt());
    }

    private static String nickname(Map<Long, User> users, Long userId) {
        User user = users.get(userId);
        return user == null ? "삭제된 유저" : user.getDisplayNickname();
    }

    private static <T> Map<Long, T> load(
            List<Report> reports,
            String type,
            Function<Collection<Long>, List<T>> loader,
            Function<T, Long> idOf) {
        List<Long> ids = reports.stream()
                .filter(report -> type.equals(report.getTargetType()))
                .map(Report::getTargetId)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return loader.apply(ids).stream().collect(Collectors.toMap(idOf, Function.identity()));
    }
}
