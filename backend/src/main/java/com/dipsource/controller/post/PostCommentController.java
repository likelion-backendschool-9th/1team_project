package com.dipsource.controller.post;

import com.dipsource.dto.post.PostCommentResponse;
import com.dipsource.dto.post.PostIdResponse;
import com.dipsource.dto.post.WriteCommentRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.post.PostCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 블로그 댓글·답글·댓글 좋아요. 작성자는 JWT에서만 가져온다. 삭제된 글이면 모두 404 */
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;

    /** 댓글 목록 (비로그인 가능). 최상위 댓글 오래된 순 + 답글 */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<PostCommentResponse>>> comments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        Long viewerId = principal == null ? null : principal.getUserId();
        return ResponseEntity.ok(ApiResponse.of(postCommentService.getComments(postId, viewerId, pageable)));
    }

    /** 댓글·답글 작성 (parentId가 있으면 답글) */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PostIdResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId,
            @Valid @RequestBody WriteCommentRequest request
    ) {
        PostIdResponse response = postCommentService.create(requireUserId(principal), postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 댓글 수정 (작성자만) */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId,
            @Valid @RequestBody WriteCommentRequest request
    ) {
        postCommentService.update(requireUserId(principal), commentId, request.content());
        return ResponseEntity.noContent().build();
    }

    /** 댓글 삭제 (작성자 또는 글 주인, soft delete) */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId
    ) {
        postCommentService.delete(requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    /** 댓글 좋아요 (멱등) */
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> like(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId
    ) {
        postCommentService.like(requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    /** 댓글 좋아요 취소 (멱등) */
    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> unlike(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId
    ) {
        postCommentService.unlike(requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
