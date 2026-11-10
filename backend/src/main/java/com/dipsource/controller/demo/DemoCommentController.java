package com.dipsource.controller.demo;

import com.dipsource.dto.demo.DemoCommentCreateRequest;
import com.dipsource.dto.demo.DemoCommentCursorResponse;
import com.dipsource.dto.demo.DemoCommentResponse;
import com.dipsource.dto.demo.DemoCommentUpdateRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.demo.DemoCommentService;
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
 * 데모 피드백(댓글) 컨트롤러
 *
 * GET  /demos/{demoId}/comments          — 목록 (커서, 비로그인 가능)
 * POST /demos/{demoId}/comments          — 작성
 * PUT  /demos/comments/{commentId}       — 수정 (본인만)
 * DELETE /demos/comments/{commentId}     — 삭제 (본인만)
 * POST·DELETE /demos/comments/{commentId}/like — 좋아요
 */
@RestController
@RequestMapping("/demos")
@RequiredArgsConstructor
public class DemoCommentController {

    private final DemoCommentService commentService;

    @GetMapping("/{demoId}/comments")
    public ResponseEntity<ApiResponse<DemoCommentCursorResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = principal == null ? null : principal.getUserId();
        return ResponseEntity.ok(ApiResponse.of(commentService.getComments(demoId, userId, cursor, size)));
    }

    @PostMapping("/{demoId}/comments")
    public ResponseEntity<ApiResponse<DemoCommentResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @Valid @RequestBody DemoCommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(commentService.create(requireUserId(principal), demoId, request)));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<DemoCommentResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId,
            @Valid @RequestBody DemoCommentUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.of(commentService.update(requireUserId(principal), commentId, request.content())));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId) {
        commentService.delete(requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> like(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId) {
        commentService.like(requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> unlike(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId) {
        commentService.unlike(requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
