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

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminContentService adminContentService;

    @GetMapping
    public ApiResponse<PageResponse<AdminContentResponse>> listComments(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.of(adminContentService.listComments(q, pageable));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId) {
        adminContentService.deleteComment(AdminAuth.requireUserId(principal), commentId);
        return ResponseEntity.noContent().build();
    }
}
