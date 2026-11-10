package com.dipsource.controller.bookmark;

import com.dipsource.dto.bookmark.MyBookmarkResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.bookmark.MyBookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 내 북마크 목록 — GET /users/me/bookmarks?type=feed|post|demo&page=&size= (StubController에서 이관) */
@RestController
@RequestMapping("/users/me/bookmarks")
@RequiredArgsConstructor
public class MyBookmarkController {

    private static final int MAX_SIZE = 100;

    private final MyBookmarkService myBookmarkService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MyBookmarkResponse>>> getMine(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        String safeType = type == null || type.isBlank() ? null : type.strip();
        return ResponseEntity.ok(ApiResponse.of(
                myBookmarkService.getMine(principal.getUserId(), safeType, safePage, safeSize)));
    }
}
