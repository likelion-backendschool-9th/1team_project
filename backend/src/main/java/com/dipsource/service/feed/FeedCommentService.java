package com.dipsource.service.feed;

import com.dipsource.dto.feed.CommentCreateRequest;
import com.dipsource.dto.feed.CommentCursorResponse;
import com.dipsource.dto.feed.CommentResponse;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedComment;
import com.dipsource.entity.feed.FeedStatus;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.feed.FeedCommentRepository;
import com.dipsource.repository.feed.FeedRepository;
import com.dipsource.repository.user.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 피드 댓글 CRUD */
@Service
@RequiredArgsConstructor
public class FeedCommentService {

    private final FeedCommentRepository feedCommentRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final TagWeightService tagWeightService;

    private static final int COMMENT_TAG_WEIGHT = 1;

    /** 댓글 목록 조회 (최상위 댓글 + 각 댓글의 대댓글 수) */
    @Transactional(readOnly = true)
    public CommentCursorResponse getComments(Long feedId, Long cursor, int size) {
        findActiveFeed(feedId);

        List<FeedComment> comments = feedCommentRepository.findTopLevelComments(feedId, cursor, size);
        if (comments.isEmpty()) {
            return CommentCursorResponse.of(List.of(), size);
        }

        // 작성자 배치 조회
        List<Long> userIds = comments.stream().map(FeedComment::getUserId).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 대댓글 수 조회
        List<CommentResponse> responses = comments.stream()
                .map(c -> {
                    User author = userMap.get(c.getUserId());
                    int replyCount = feedCommentRepository.findReplies(c.getId()).size();
                    return CommentResponse.from(c, author, replyCount);
                })
                .toList();

        return CommentCursorResponse.of(responses, size);
    }

    /** 대댓글 목록 조회 */
    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(Long commentId) {
        FeedComment parent = findComment(commentId);

        List<FeedComment> replies = feedCommentRepository.findReplies(parent.getId());
        if (replies.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = replies.stream().map(FeedComment::getUserId).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return replies.stream()
                .map(r -> CommentResponse.from(r, userMap.get(r.getUserId())))
                .toList();
    }

    /** 댓글 작성 */
    @Transactional
    public CommentResponse create(Long userId, Long feedId, CommentCreateRequest request) {
        Feed feed = findActiveFeed(feedId);
        User author = requireUser(userId);

        FeedComment parent = null;
        if (request.parentId() != null) {
            parent = findComment(request.parentId());
            // 대댓글의 대댓글 방지 (1단계만 허용)
            if (parent.getParent() != null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "대댓글에는 답글을 달 수 없습니다");
            }
        }

        FeedComment comment = FeedComment.builder()
                .feed(feed)
                .userId(userId)
                .parent(parent)
                .content(request.content())
                .build();
        feedCommentRepository.save(comment);
        feedCommentRepository.incrementCommentCount(feedId);
        tagWeightService.increaseWeight(author, feedId, COMMENT_TAG_WEIGHT);

        return CommentResponse.from(comment, author);
    }

    /** 댓글 수정 (본인만) */
    @Transactional
    public CommentResponse update(Long userId, Long commentId, String content) {
        FeedComment comment = findOwnComment(userId, commentId);
        comment.update(content);
        User author = requireUser(userId);
        return CommentResponse.from(comment, author);
    }

    /** 댓글 삭제 (본인만, 소프트 삭제) */
    @Transactional
    public void delete(Long userId, Long commentId) {
        FeedComment comment = findOwnComment(userId, commentId);
        comment.delete();
        feedCommentRepository.decrementCommentCount(comment.getFeed().getId());
        tagWeightService.decreaseWeight(requireUser(userId), comment.getFeed().getId(), COMMENT_TAG_WEIGHT);
    }

    private Feed findActiveFeed(Long feedId) {
        return feedRepository.findById(feedId)
                .filter(f -> f.getStatus() == FeedStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "피드를 찾을 수 없습니다"));
    }

    private FeedComment findComment(Long commentId) {
        return feedCommentRepository.findById(commentId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다"));
    }

    private FeedComment findOwnComment(Long userId, Long commentId) {
        FeedComment comment = findComment(commentId);
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 댓글만 수정/삭제할 수 있습니다");
        }
        return comment;
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다"));
    }
}
