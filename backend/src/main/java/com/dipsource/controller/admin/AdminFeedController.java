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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/admin/feeds")
@RequiredArgsConstructor
public class AdminFeedController {

    private final AdminContentService adminContentService;

    @GetMapping
    public ApiResponse<PageResponse<AdminContentResponse>> listFeeds(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.of(adminContentService.listFeeds(q, pageable));
    }

    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long feedId) {
        adminContentService.deleteFeed(AdminAuth.requireUserId(principal), feedId);
        return ResponseEntity.noContent().build();
    }
}