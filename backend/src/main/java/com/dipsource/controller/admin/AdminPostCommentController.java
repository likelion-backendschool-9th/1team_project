package com.dipsource.controller.admin;

import com.dipsource.dto.admin.AdminContentResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.service.admin.AdminContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 블로그 댓글 관리. 피드 댓글(/admin/comments)과 테이블이 달라 ID가 겹칠 수 있어 경로를 나눈다. */
@RestController
@RequestMapping("/admin/post-comments")
@RequiredArgsConstructor
public class AdminPostCommentController {

    private final AdminContentService adminContentService;

    @GetMapping
    public ApiResponse<PageResponse<AdminContentResponse>> listPostComments(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.of(adminContentService.listPostComments(q, pageable));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deletePostComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId) {
        adminContentService.deletePostComment(AdminAuth.requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }
}
