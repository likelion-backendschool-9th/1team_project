package com.dipsource.controller.feed;

import com.dipsource.dto.feed.CommentCreateRequest;
import com.dipsource.dto.feed.CommentCursorResponse;
import com.dipsource.dto.feed.CommentResponse;
import com.dipsource.dto.feed.CommentUpdateRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.feed.FeedCommentService;
import jakarta.validation.Valid;
import java.util.List;
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
 * 피드 댓글 컨트롤러
 *
 * GET /api/v1/feeds/{feedId}/comments — 댓글 목록
 * POST /api/v1/feeds/{feedId}/comments — 댓글 작성 (인증 필요)
 * GET /api/v1/feeds/comments/{commentId}/replies — 대댓글 목록
 * PUT /api/v1/feeds/comments/{commentId} — 댓글 수정 (인증 필요, 본인만)
 * DELETE /api/v1/feeds/comments/{commentId} — 댓글 삭제 (인증 필요, 본인만)
 * POST /api/v1/feeds/comments/{commentId}/like — 댓글 좋아요 (인증 필요)
 * DELETE /api/v1/feeds/comments/{commentId}/like — 댓글 좋아요 취소 (인증 필요)
 */
@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
public class FeedCommentController {

    private static final int MAX_SIZE = 100;

    private final FeedCommentService feedCommentService;

    /** 댓글 목록 (커서 페이지네이션) */
    @GetMapping("/{feedId}/comments")
    public ResponseEntity<ApiResponse<CommentCursorResponse>> getComments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long feedId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = principal == null ? null : principal.getUserId();
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        CommentCursorResponse response = feedCommentService.getComments(feedId, userId, cursor, safeSize);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /** 댓글 작성 */
    @PostMapping("/{feedId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long feedId,
            @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = feedCommentService.create(requireUserId(principal), feedId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 대댓글 목록 */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getReplies(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId) {
        Long userId = principal == null ? null : principal.getUserId();
        List<CommentResponse> responses = feedCommentService.getReplies(commentId, userId);
        return ResponseEntity.ok(ApiResponse.of(responses));
    }

    /** 댓글 수정 (본인만) */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {
        CommentResponse response = feedCommentService.update(requireUserId(principal), commentId, request.content());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /** 댓글 삭제 (본인만, 소프트 삭제) */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId) {
        feedCommentService.delete(requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    /** 댓글 좋아요 */
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId) {
        feedCommentService.likeComment(requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    /** 댓글 좋아요 취소 */
    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> unlikeComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId) {
        feedCommentService.unlikeComment(requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
