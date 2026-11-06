package com.dipsource.service.post.impl;

import com.dipsource.dto.post.PostCommentResponse;
import com.dipsource.dto.post.PostIdResponse;
import com.dipsource.dto.post.WriteCommentRequest;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostComment;
import com.dipsource.entity.post.PostStatus;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.post.PostCommentLikeRepository;
import com.dipsource.repository.post.PostCommentRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.service.post.PostCommentService;
import com.dipsource.service.user.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link PostCommentService} 구현체. */
@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    /** 최상위 댓글 한 번에 최대 몇 개까지 주는지 */
    private static final int MAX_PAGE_SIZE = 50;

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostCommentLikeRepository postCommentLikeRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostCommentResponse> getComments(Long postId, Long viewerId, Pageable pageable) {
        findActivePost(postId);
        Page<PostComment> page = postCommentRepository.findTopLevel(
                postId, PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE)));
        List<Long> topIds = page.getContent().stream().map(PostComment::getId).toList();
        List<PostComment> replies = topIds.isEmpty() ? List.of() : postCommentRepository.findReplies(topIds);

        List<PostComment> all = Stream.concat(page.getContent().stream(), replies.stream()).toList();
        Map<Long, UserSummaryResponse> authors =
                userService.getSummaries(all.stream().map(PostComment::getUserId).distinct().toList());
        Set<Long> liked = likedIds(viewerId, all);

        // 답글을 부모별로 모은다 (쿼리가 오래된 순이라 순서가 유지된다).
        Map<Long, List<PostCommentResponse>> repliesByParent = new LinkedHashMap<>();
        for (PostComment reply : replies) {
            repliesByParent.computeIfAbsent(reply.getParent().getId(), id -> new ArrayList<>())
                    .add(PostCommentResponse.of(reply, authors.get(reply.getUserId()), liked.contains(reply.getId()), List.of()));
        }
        return PageResponse.of(page, comment -> PostCommentResponse.of(
                comment,
                authors.get(comment.getUserId()),
                liked.contains(comment.getId()),
                repliesByParent.getOrDefault(comment.getId(), List.of())));
    }

    @Override
    @Transactional
    public PostIdResponse create(Long userId, Long postId, WriteCommentRequest request) {
        Post post = findActivePost(postId);
        if (!userService.isActiveUser(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "댓글을 쓸 수 없는 계정이에요.");
        }
        PostComment parent = request.parentId() == null ? null : findReplyTarget(postId, request.parentId());
        PostComment comment = postCommentRepository.save(PostComment.builder()
                .post(post)
                .userId(userId)
                .parent(parent)
                .content(request.content().strip())
                .build());
        postRepository.incrementCommentCount(postId);
        return new PostIdResponse(comment.getId());
    }

    @Override
    @Transactional
    public void update(Long userId, Long commentId, String content) {
        PostComment comment = findWritableComment(commentId);
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "내 댓글만 수정할 수 있어요.");
        }
        comment.update(content.strip());
    }

    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        PostComment comment = findWritableComment(commentId);
        boolean postOwner = comment.getPost().getUserId().equals(userId);
        if (!comment.getUserId().equals(userId) && !postOwner) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "내 댓글이나 내 글의 댓글만 삭제할 수 있어요.");
        }
        comment.delete();
        postRepository.decrementCommentCount(comment.getPost().getId());
    }

    @Override
    @Transactional
    public void like(Long userId, Long commentId) {
        findWritableComment(commentId);
        if (postCommentLikeRepository.insertIgnore(userId, commentId) > 0) {
            postCommentRepository.incrementLikeCount(commentId);
        }
    }

    @Override
    @Transactional
    public void unlike(Long userId, Long commentId) {
        findWritableComment(commentId);
        if (postCommentLikeRepository.deleteByUserIdAndCommentId(userId, commentId) > 0) {
            postCommentRepository.decrementLikeCount(commentId);
        }
    }

    /** 답글을 달 수 있는 부모: 같은 글의, 지우지 않은 최상위 댓글 */
    private PostComment findReplyTarget(Long postId, Long parentId) {
        PostComment parent = postCommentRepository.findById(parentId)
                .filter(comment -> comment.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "답글을 달 댓글을 찾을 수 없어요."));
        if (!parent.getPost().getId().equals(postId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "다른 글의 댓글에는 답글을 달 수 없어요.");
        }
        if (parent.getParent() != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "답글에는 다시 답글을 달 수 없어요.");
        }
        return parent;
    }

    /** 수정·삭제·좋아요할 수 있는 댓글: 지우지 않았고, 글이 공개 상태 */
    private PostComment findWritableComment(Long commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없어요."));
        if (comment.getPost().getStatus() != PostStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "글을 찾을 수 없어요.");
        }
        return comment;
    }

    private Post findActivePost(Long postId) {
        return postRepository.findById(postId)
                .filter(post -> post.getStatus() == PostStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "글을 찾을 수 없어요."));
    }

    private Set<Long> likedIds(Long viewerId, List<PostComment> comments) {
        if (viewerId == null || comments.isEmpty()) {
            return Set.of();
        }
        List<Long> ids = comments.stream().map(PostComment::getId).collect(Collectors.toList());
        return new HashSet<>(postCommentLikeRepository.findLikedCommentIds(viewerId, ids));
    }
}
