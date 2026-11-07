package com.dipsource.service.admin.impl;

import com.dipsource.dto.admin.AdminContentResponse;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.user.User;
import com.dipsource.global.common.PageResponse;
import com.dipsource.repository.feed.FeedRepository;
import com.dipsource.repository.report.ReportRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.admin.AdminContentService;
import com.dipsource.service.feed.FeedService;
import com.dipsource.entity.admin.AdminActionLog;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.admin.AdminActionLogRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminContentServiceImpl implements AdminContentService {

    private static final int MAX_PAGE_SIZE = 100;

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final FeedService feedService;
    private final AdminActionLogRepository adminActionLogRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminContentResponse> listFeeds(String q, Pageable pageable) {
        int size = pageable.getPageSize() <= 0 ? 10 : Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable limited = PageRequest.of(pageable.getPageNumber(), size);
        Page<Feed> page = feedRepository.searchForAdmin(likePattern(q), limited);
        List<Feed> feeds = page.getContent();
        Map<Long, String> nicknames = nicknamesOf(feeds);
        Map<Long, Integer> reported = reportedOf(feeds);
        return PageResponse.of(page, feed -> AdminContentResponse.ofFeed(
                feed,
                nicknames.getOrDefault(feed.getUserId(), "삭제된 유저"),
                reported.getOrDefault(feed.getId(), 0)));
    }

    @Override
    @Transactional
    public void deleteFeed(Long adminId, Long feedId) {
        feedService.deleteByAdmin(feedId);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "관리자를 찾을 수 없습니다"));
        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(admin)
                .actionType("DELETE")
                .targetType("FEED")
                .targetId(feedId)
                .build());
    }

    private Map<Long, String> nicknamesOf(List<Feed> feeds) {
        List<Long> userIds = feeds.stream().map(Feed::getUserId).distinct().toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayNickname));
    }

    private Map<Long, Integer> reportedOf(List<Feed> feeds) {
        List<Long> ids = feeds.stream().map(Feed::getId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return reportRepository.countByTargets("FEED", ids).stream()
                .collect(Collectors.toMap(
                        ReportRepository.TargetCount::getTargetId,
                        count -> (int) count.getCnt()));
    }

    private static String likePattern(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        String escaped = q.strip().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}