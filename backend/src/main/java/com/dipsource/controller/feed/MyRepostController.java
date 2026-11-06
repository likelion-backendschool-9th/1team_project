package com.dipsource.controller.feed;

import com.dipsource.dto.feed.FeedCursorResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.feed.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 내가 리포스트한 피드 목록 조회 — GET /api/v1/users/me/reposts */
@RestController
@RequestMapping("/api/v1/users/me/reposts")
@RequiredArgsConstructor
public class MyRepostController {

    private static final int MAX_SIZE = 100;

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<ApiResponse<FeedCursorResponse>> getMyReposts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        FeedCursorResponse response = feedService.getMyReposts(principal.getUserId(), cursor, safeSize);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
