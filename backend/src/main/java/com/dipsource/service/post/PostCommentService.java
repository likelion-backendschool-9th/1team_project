package com.dipsource.service.post;

import com.dipsource.dto.post.PostCommentCreateRequest;
import com.dipsource.dto.post.PostCommentCursorResponse;
import com.dipsource.dto.post.PostCommentResponse;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostComment;
import com.dipsource.entity.post.PostStatus;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.post.PostCommentLikeRepository;
import com.dipsource.repository.post.PostCommentRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.user.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 블로그 댓글 CRUD */
@Service
@RequiredArgsConstructor
public class PostCommentService {

    private static final int MAX_SIZE = 100;

    private final PostCommentRepository postCommentRepository;
    private final PostCommentLikeRepository postCommentLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;

    /** 댓글 목록 조회 (최상위 댓글 + 대댓글 포함, 커서 페이지네이션) */
    @Transactional(readOnly = true)
    public PostCommentCursorResponse getComments(Long postId, Long userId, Long cursor, int size) {
        Post post = findActivePost(postId);
        Set<Long> hidden = hiddenUserIds(userId);
        if (hidden.contains(post.getUserId())) {
            return PostCommentCursorResponse.of(List.of(), false);
        }
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);

        // size+1개 조회 — 초과 여부로 hasNext 판단 (DB에서 이미 visible 필터 적용)
        List<PostComment> fetched = postCommentRepository.findTopLevelCommentsVisible(
                postId, cursor, hidden.toArray(Long[]::new), safeSize + 1);
        boolean hasNext = fetched.size() > safeSize;
        List<PostComment> comments = hasNext ? fetched.subList(0, safeSize) : fetched;

        if (comments.isEmpty()) {
            return PostCommentCursorResponse.of(List.of(), false);
        }

        // 대댓글 배치 조회
        List<Long> commentIds = comments.stream().map(PostComment::getId).toList();
        List<PostComment> allReplies = postCommentRepository.findRepliesByParentIds(commentIds).stream()
                .filter(r -> !hidden.contains(r.getUserId()))
                .toList();

        // 모든 댓글 ID — 좋아요 상태 조회용
        List<Long> allCommentIds = Stream.concat(
                commentIds.stream(),
                allReplies.stream().map(PostComment::getId)
        ).toList();

        Set<Long> likedCommentIds = userId != null && !allCommentIds.isEmpty()
                ? new HashSet<>(postCommentLikeRepository.findLikedCommentIds(userId, allCommentIds))
                : Set.of();

        // 작성자 배치 조회
        List<Long> allUserIds = Stream.concat(
                comments.stream().map(PostComment::getUserId),
                allReplies.stream().map(PostComment::getUserId)
        ).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 대댓글을 부모별로 그룹핑
        Map<Long, List<PostComment>> replyMap = allReplies.stream()
                .collect(Collectors.groupingBy(r -> r.getParent().getId()));

        List<PostCommentResponse> responses = comments.stream()
                .map(c -> {
                    List<PostComment> replies = replyMap.getOrDefault(c.getId(), List.of());
                    List<PostCommentResponse> replyResponses = replies.stream()
                            .map(r -> PostCommentResponse.from(r, userMap.get(r.getUserId()), likedCommentIds.contains(r.getId())))
                            .toList();
                    return PostCommentResponse.from(c, userMap.get(c.getUserId()), likedCommentIds.contains(c.getId()), replyResponses);
                })
                .toList();

        return PostCommentCursorResponse.of(responses, hasNext);
    }

    /** 댓글 작성 */
    @Transactional
    public PostCommentResponse create(Long userId, Long postId, PostCommentCreateRequest request) {
        Post post = findActivePost(postId);
        requireNotBlocked(userId, post.getUserId(), "글을 찾을 수 없습니다");
        User author = requireUser(userId);

        PostComment parent = null;
        if (request.parentId() != null) {
            parent = findComment(request.parentId());
            requireNotBlocked(userId, parent.getUserId(), "댓글을 찾을 수 없습니다");
            if (!parent.getPost().getId().equals(postId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "다른 글의 댓글에는 답글을 달 수 없습니다");
            }
            if (parent.getParent() != null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "대댓글에는 답글을 달 수 없습니다");
            }
        }

        PostComment comment = PostComment.builder()
                .post(post)
                .userId(userId)
                .parent(parent)
                .content(request.content())
                .build();
        postCommentRepository.save(comment);
        postCommentRepository.incrementCommentCount(postId);

        return PostCommentResponse.from(comment, author, false);
    }

    /** 댓글 수정 (본인만) */
    @Transactional
    public PostCommentResponse update(Long userId, Long commentId, String content) {
        PostComment comment = findOwnComment(userId, commentId);
        comment.update(content);
        User author = requireUser(userId);
        return PostCommentResponse.from(comment, author, false);
    }

    /** 댓글 삭제 (본인만, 소프트 삭제)
     * softDeleteIfActive로 원자적 처리 — 동시 삭제 요청이 와도 commentCount 중복 감소 방지 */
    @Transactional
    public void delete(Long userId, Long commentId) {
        PostComment comment = findCommentIncludeDeleted(commentId);
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 댓글만 삭제할 수 있습니다");
        }
        int updated = postCommentRepository.softDeleteIfActive(commentId);
        if (updated > 0) {
            postCommentRepository.decrementCommentCount(comment.getPost().getId());
        }
    }

    /** 관리자 삭제. 작성자 검사 없이 소프트 삭제하고 글 댓글 수를 맞춘다. 이미 삭제된 댓글이면 404 */
    @Transactional
    public void deleteByAdmin(Long commentId) {
        PostComment comment = findComment(commentId);
        int updated = postCommentRepository.softDeleteIfActive(commentId);
        if (updated > 0) {
            postCommentRepository.decrementCommentCount(comment.getPost().getId());
        }
    }

    /** 댓글 좋아요. 이미 눌렀으면 no-op (멱등) */
    @Transactional
    public void likeComment(Long userId, Long commentId) {
        PostComment comment = findComment(commentId);
        requireNotBlocked(userId, comment.getUserId(), "댓글을 찾을 수 없습니다");
        int inserted = postCommentLikeRepository.insertIgnore(userId, commentId);
        if (inserted > 0) {
            postCommentRepository.incrementCommentLikeCount(commentId);
        }
    }

    /** 댓글 좋아요 취소. 없으면 no-op (멱등). 삭제된 댓글도 취소 허용 */
    @Transactional
    public void unlikeComment(Long userId, Long commentId) {
        findCommentIncludeDeleted(commentId);
        int deleted = postCommentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);
        if (deleted > 0) {
            postCommentRepository.decrementCommentLikeCount(commentId);
        }
    }

    private Set<Long> hiddenUserIds(Long userId) {
        return userId == null ? Set.of() : new HashSet<>(blockRepository.findHiddenUserIds(userId));
    }

    /** 차단 여부가 드러나지 않도록 없는 대상과 같은 응답을 준다 */
    private void requireNotBlocked(Long userId, Long targetUserId, String notFoundMessage) {
        if (blockRepository.existsBetween(userId, targetUserId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, notFoundMessage);
        }
    }

    private Post findActivePost(Long postId) {
        return postRepository.findById(postId)
                .filter(p -> p.getStatus() == PostStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "글을 찾을 수 없습니다"));
    }

    private PostComment findComment(Long commentId) {
        return postCommentRepository.findById(commentId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다"));
    }

    private PostComment findCommentIncludeDeleted(Long commentId) {
        return postCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다"));
    }

    private PostComment findOwnComment(Long userId, Long commentId) {
        PostComment comment = findComment(commentId);
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
