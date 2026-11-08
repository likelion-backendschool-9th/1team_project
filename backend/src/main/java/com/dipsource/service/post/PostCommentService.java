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
import com.dipsource.repository.post.PostCommentLikeRepository;
import com.dipsource.repository.post.PostCommentRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.user.UserRepository;
import java.util.ArrayList;
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

    /** 댓글 목록 조회 (최상위 댓글 + 대댓글 포함, 커서 페이지네이션) */
    @Transactional(readOnly = true)
    public PostCommentCursorResponse getComments(Long postId, Long userId, Long cursor, int size) {
        findActivePost(postId);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);

        List<PostComment> comments = postCommentRepository.findTopLevelCommentsIncludeDeleted(postId, cursor, safeSize);
        if (comments.isEmpty()) {
            return PostCommentCursorResponse.of(List.of(), safeSize);
        }

        // 대댓글 배치 조회
        List<Long> commentIds = comments.stream().map(PostComment::getId).toList();
        List<PostComment> allReplies = postCommentRepository.findRepliesByParentIds(commentIds);

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

        // 삭제됐고 대댓글도 없는 댓글은 제외
        List<PostCommentResponse> responses = new ArrayList<>();
        for (PostComment c : comments) {
            List<PostComment> replies = replyMap.getOrDefault(c.getId(), List.of());
            if (c.getDeletedAt() != null && replies.isEmpty()) {
                continue;
            }
            List<PostCommentResponse> replyResponses = replies.stream()
                    .map(r -> PostCommentResponse.from(r, userMap.get(r.getUserId()), likedCommentIds.contains(r.getId())))
                    .toList();
            responses.add(PostCommentResponse.from(c, userMap.get(c.getUserId()), likedCommentIds.contains(c.getId()), replyResponses));
        }

        return PostCommentCursorResponse.of(responses, safeSize);
    }

    /** 댓글 작성 */
    @Transactional
    public PostCommentResponse create(Long userId, Long postId, PostCommentCreateRequest request) {
        Post post = findActivePost(postId);
        User author = requireUser(userId);

        PostComment parent = null;
        if (request.parentId() != null) {
            parent = findComment(request.parentId());
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

    /** 댓글 삭제 (본인만, 소프트 삭제) */
    @Transactional
    public void delete(Long userId, Long commentId) {
        PostComment comment = findOwnComment(userId, commentId);
        comment.delete();
        postCommentRepository.decrementCommentCount(comment.getPost().getId());
    }

    /** 댓글 좋아요. 이미 눌렀으면 no-op (멱등) */
    @Transactional
    public void likeComment(Long userId, Long commentId) {
        findComment(commentId);
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
