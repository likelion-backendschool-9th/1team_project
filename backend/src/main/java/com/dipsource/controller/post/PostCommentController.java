package com.dipsource.controller.post;

import com.dipsource.dto.post.PostCommentCreateRequest;
import com.dipsource.dto.post.PostCommentCursorResponse;
import com.dipsource.dto.post.PostCommentResponse;
import com.dipsource.dto.post.PostCommentUpdateRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.post.PostCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 블로그 댓글 컨트롤러
 *
 * GET  /posts/{postId}/comments           — 댓글 목록 (비로그인 가능)
 * POST /posts/{postId}/comments           — 댓글 작성 (인증 필요)
 * PUT  /posts/comments/{commentId}        — 댓글 수정 (인증 필요, 본인만)
 * DELETE /posts/comments/{commentId}      — 댓글 삭제 (인증 필요, 본인만)
 * POST /posts/comments/{commentId}/like   — 댓글 좋아요 (인증 필요)
 * DELETE /posts/comments/{commentId}/like — 댓글 좋아요 취소 (인증 필요)
 */
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;

    /** 댓글 목록 (커서 페이지네이션, 비로그인 가능) */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PostCommentCursorResponse>> getComments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = principal == null ? null : principal.getUserId();
        PostCommentCursorResponse response = postCommentService.getComments(postId, userId, cursor, size);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /** 댓글 작성 */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PostCommentResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId,
            @Valid @RequestBody PostCommentCreateRequest request) {
        PostCommentResponse response = postCommentService.create(requireUserId(principal), postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 댓글 수정 (본인만) */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<PostCommentResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId,
            @Valid @RequestBody PostCommentUpdateRequest request) {
        PostCommentResponse response = postCommentService.update(requireUserId(principal), commentId, request.content());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /** 댓글 삭제 (본인만, 소프트 삭제) */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId) {
        postCommentService.delete(requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    /** 댓글 좋아요 */
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId) {
        postCommentService.likeComment(requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    /** 댓글 좋아요 취소 */
    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> unlikeComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId) {
        postCommentService.unlikeComment(requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
