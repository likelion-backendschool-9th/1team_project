package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoCommentCreateRequest;
import com.dipsource.dto.demo.DemoCommentCursorResponse;
import com.dipsource.dto.demo.DemoCommentResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoFeedback;
import com.dipsource.entity.demo.DemoFeedbackType;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.demo.DemoFeedbackLikeRepository;
import com.dipsource.repository.demo.DemoFeedbackRepository;
import com.dipsource.repository.demo.DemoRepository;
import com.dipsource.repository.user.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 데모 피드백(댓글) CRUD. 블로그 댓글과 같은 규칙을 따른다. */
@Service
@RequiredArgsConstructor
public class DemoCommentService {

    private static final int MAX_SIZE = 100;
    private static final String NOT_FOUND = "피드백을 찾을 수 없습니다";

    private final DemoFeedbackRepository feedbackRepository;
    private final DemoFeedbackLikeRepository feedbackLikeRepository;
    private final DemoRepository demoRepository;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final DemoAccess demoAccess;

    @Transactional(readOnly = true)
    public DemoCommentCursorResponse getComments(Long demoId, Long userId, Long cursor, int size) {
        demoAccess.findVisible(demoId, userId);
        Set<Long> hidden = userId == null ? Set.of() : new HashSet<>(blockRepository.findHiddenUserIds(userId));
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);

        List<DemoFeedback> fetched = feedbackRepository.findTopLevelVisible(
                demoId, cursor, hidden.toArray(Long[]::new), safeSize + 1);
        boolean hasNext = fetched.size() > safeSize;
        List<DemoFeedback> comments = hasNext ? fetched.subList(0, safeSize) : fetched;
        if (comments.isEmpty()) {
            return DemoCommentCursorResponse.of(List.of(), false);
        }

        List<Long> commentIds = comments.stream().map(DemoFeedback::getId).toList();
        List<DemoFeedback> replies = feedbackRepository.findRepliesByParentIds(commentIds).stream()
                .filter(reply -> !hidden.contains(reply.getUserId()))
                .toList();

        List<Long> allIds = Stream.concat(commentIds.stream(), replies.stream().map(DemoFeedback::getId)).toList();
        Set<Long> liked = userId == null ? Set.of() : new HashSet<>(feedbackLikeRepository.findLikedFeedbackIds(userId, allIds));
        Map<Long, User> users = userRepository.findAllById(
                        Stream.concat(comments.stream(), replies.stream()).map(DemoFeedback::getUserId).distinct().toList())
                .stream().collect(Collectors.toMap(User::getId, user -> user));
        Map<Long, List<DemoFeedback>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(reply -> reply.getParent().getId()));

        List<DemoCommentResponse> responses = comments.stream().map(comment -> {
            List<DemoCommentResponse> replyResponses = replyMap.getOrDefault(comment.getId(), List.of()).stream()
                    .map(reply -> DemoCommentResponse.of(reply, users.get(reply.getUserId()), liked.contains(reply.getId()), null))
                    .toList();
            return DemoCommentResponse.of(comment, users.get(comment.getUserId()), liked.contains(comment.getId()), replyResponses);
        }).toList();
        return DemoCommentCursorResponse.of(responses, hasNext);
    }

    @Transactional
    public DemoCommentResponse create(Long userId, Long demoId, DemoCommentCreateRequest request) {
        Demo demo = demoAccess.findActive(demoId, userId);
        User author = requireUser(userId);

        DemoFeedback parent = null;
        if (request.parentId() != null) {
            parent = findComment(request.parentId());
            requireNotBlocked(userId, parent.getUserId());
            if (!parent.getDemo().getId().equals(demoId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "다른 데모의 피드백에는 답글을 달 수 없습니다");
            }
            if (parent.getParent() != null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "답글에는 답글을 달 수 없습니다");
            }
        }

        DemoFeedback comment = feedbackRepository.save(DemoFeedback.builder()
                .demo(demo)
                .userId(userId)
                .parent(parent)
                .content(request.content())
                .type(parseType(request.type()))
                .build());
        demoRepository.incrementFeedbackCount(demoId);
        return DemoCommentResponse.of(comment, author, false, List.of());
    }

    @Transactional
    public DemoCommentResponse update(Long userId, Long commentId, String content) {
        DemoFeedback comment = findComment(commentId);
        requireAuthor(comment, userId);
        comment.update(content);
        return DemoCommentResponse.of(comment, requireUser(userId), false, List.of());
    }

    /** 동시 삭제에도 피드백 수가 한 번만 줄어든다. */
    @Transactional
    public void delete(Long userId, Long commentId) {
        DemoFeedback comment = feedbackRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, NOT_FOUND));
        requireAuthor(comment, userId);
        Long demoId = comment.getDemo().getId();
        if (feedbackRepository.softDeleteIfActive(commentId) > 0) {
            demoRepository.decrementFeedbackCount(demoId);
        }
    }

    @Transactional
    public void like(Long userId, Long commentId) {
        DemoFeedback comment = findComment(commentId);
        requireNotBlocked(userId, comment.getUserId());
        if (feedbackLikeRepository.insertIgnore(userId, commentId) > 0) {
            feedbackRepository.incrementLikeCount(commentId);
        }
    }

    @Transactional
    public void unlike(Long userId, Long commentId) {
        if (!feedbackRepository.existsById(commentId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, NOT_FOUND);
        }
        if (feedbackLikeRepository.deleteByUserIdAndFeedbackId(userId, commentId) > 0) {
            feedbackRepository.decrementLikeCount(commentId);
        }
    }

    private DemoFeedbackType parseType(String type) {
        if (type == null || type.isBlank()) {
            return DemoFeedbackType.PRAISE;
        }
        try {
            return DemoFeedbackType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "피드백 종류가 올바르지 않습니다");
        }
    }

    private void requireAuthor(DemoFeedback comment, Long userId) {
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 피드백만 수정/삭제할 수 있습니다");
        }
    }

    /** 차단 여부가 드러나지 않도록 없는 대상과 같은 응답을 준다 */
    private void requireNotBlocked(Long userId, Long targetUserId) {
        if (blockRepository.existsBetween(userId, targetUserId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, NOT_FOUND);
        }
    }

    private DemoFeedback findComment(Long commentId) {
        return feedbackRepository.findById(commentId)
                .filter(comment -> comment.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, NOT_FOUND));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다"));
    }
}
