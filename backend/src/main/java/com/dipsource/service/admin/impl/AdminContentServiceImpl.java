package com.dipsource.service.admin.impl;

import com.dipsource.dto.admin.AdminContentResponse;
import com.dipsource.entity.admin.AdminActionLog;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoStatus;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedComment;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostComment;
import com.dipsource.entity.user.User;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.admin.AdminActionLogRepository;
import com.dipsource.repository.demo.DemoRepository;
import com.dipsource.repository.feed.FeedCommentRepository;
import com.dipsource.repository.feed.FeedRepository;
import com.dipsource.repository.post.PostCommentRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.report.ReportRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.admin.AdminContentService;
import com.dipsource.service.feed.FeedCommentService;
import com.dipsource.service.feed.FeedService;
import com.dipsource.service.post.PostCommentService;
import com.dipsource.service.post.PostService;
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
    private final PostRepository postRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final PostCommentRepository postCommentRepository;
    private final DemoRepository demoRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final FeedService feedService;
    private final PostService postService;
    private final FeedCommentService feedCommentService;
    private final PostCommentService postCommentService;
    private final AdminActionLogRepository adminActionLogRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminContentResponse> listFeeds(String q, Pageable pageable) {
        Page<Feed> page = feedRepository.searchForAdmin(likePattern(q), limited(pageable));
        Map<Long, String> nicknames = nicknamesOf(page.getContent().stream().map(Feed::getUserId).toList());
        Map<Long, Integer> reported = reportedOf("FEED", page.getContent().stream().map(Feed::getId).toList());
        return PageResponse.of(page, feed -> AdminContentResponse.ofFeed(
                feed,
                nicknames.getOrDefault(feed.getUserId(), "삭제된 유저"),
                reported.getOrDefault(feed.getId(), 0)));
    }

    @Override
    @Transactional
    public void deleteFeed(Long adminId, Long feedId) {
        feedService.deleteByAdmin(feedId);
        logDelete(adminId, "FEED", feedId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminContentResponse> listPosts(String q, Pageable pageable) {
        Page<Post> page = postRepository.searchForAdmin(likePattern(q), limited(pageable));
        Map<Long, String> nicknames = nicknamesOf(page.getContent().stream().map(Post::getUserId).toList());
        Map<Long, Integer> reported = reportedOf("POST", page.getContent().stream().map(Post::getId).toList());
        return PageResponse.of(page, post -> AdminContentResponse.ofPost(
                post,
                nicknames.getOrDefault(post.getUserId(), "삭제된 유저"),
                reported.getOrDefault(post.getId(), 0)));
    }

    @Override
    @Transactional
    public void deletePost(Long adminId, Long postId) {
        postService.deleteByAdmin(postId);
        logDelete(adminId, "POST", postId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminContentResponse> listComments(String q, Pageable pageable) {
        Page<FeedComment> page = feedCommentRepository.searchForAdmin(likePattern(q), limited(pageable));
        Map<Long, String> nicknames = nicknamesOf(page.getContent().stream().map(FeedComment::getUserId).toList());
        Map<Long, Integer> reported = reportedOf(
                "FEED_COMMENT", page.getContent().stream().map(FeedComment::getId).toList());
        return PageResponse.of(page, comment -> AdminContentResponse.ofComment(
                comment,
                nicknames.getOrDefault(comment.getUserId(), "삭제된 유저"),
                reported.getOrDefault(comment.getId(), 0)));
    }

    @Override
    @Transactional
    public void deleteComment(Long adminId, Long commentId) {
        feedCommentService.deleteByAdmin(commentId);
        logDelete(adminId, "FEED_COMMENT", commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminContentResponse> listPostComments(String q, Pageable pageable) {
        Page<PostComment> page = postCommentRepository.searchForAdmin(likePattern(q), limited(pageable));
        Map<Long, String> nicknames = nicknamesOf(page.getContent().stream().map(PostComment::getUserId).toList());
        // 블로그 댓글 신고는 아직 없어서 0이지만, 신고가 생기면 이 타입으로 센다.
        Map<Long, Integer> reported = reportedOf(
                "POST_COMMENT", page.getContent().stream().map(PostComment::getId).toList());
        return PageResponse.of(page, comment -> AdminContentResponse.ofPostComment(
                comment,
                nicknames.getOrDefault(comment.getUserId(), "삭제된 유저"),
                reported.getOrDefault(comment.getId(), 0)));
    }

    @Override
    @Transactional
    public void deletePostComment(Long adminId, Long commentId) {
        postCommentService.deleteByAdmin(commentId);
        logDelete(adminId, "POST_COMMENT", commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminContentResponse> listDemos(String q, Pageable pageable) {
        Page<Demo> page = demoRepository.searchForAdmin(likePattern(q), limited(pageable));
        Map<Long, String> nicknames = nicknamesOf(page.getContent().stream().map(Demo::getUserId).toList());
        Map<Long, Integer> reported = reportedOf("DEMO", page.getContent().stream().map(Demo::getId).toList());
        return PageResponse.of(page, demo -> AdminContentResponse.ofDemo(
                demo,
                nicknames.getOrDefault(demo.getUserId(), "삭제된 유저"),
                reported.getOrDefault(demo.getId(), 0)));
    }

    @Override
    @Transactional
    public void deleteDemo(Long adminId, Long demoId) {
        Demo demo = demoRepository.findById(demoId)
                .filter(d -> d.getStatus() != DemoStatus.DELETED)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "데모를 찾을 수 없습니다"));
        demo.delete();
        logDelete(adminId, "DEMO", demoId);
    }

    private void logDelete(Long adminId, String targetType, Long targetId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "관리자를 찾을 수 없습니다"));
        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(admin)
                .actionType("DELETE")
                .targetType(targetType)
                .targetId(targetId)
                .build());
    }

    private Map<Long, String> nicknamesOf(List<Long> userIds) {
        List<Long> distinct = userIds.stream().distinct().toList();
        if (distinct.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(distinct).stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayNickname));
    }

    private Map<Long, Integer> reportedOf(String targetType, List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return reportRepository.countByTargets(targetType, ids).stream()
                .collect(Collectors.toMap(
                        ReportRepository.TargetCount::getTargetId,
                        count -> (int) count.getCnt(),
                        Integer::sum));
    }

    private Pageable limited(Pageable pageable) {
        int size = pageable.getPageSize() <= 0 ? 10 : Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        return PageRequest.of(pageable.getPageNumber(), size);
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
