package com.dipsource.controller.feed;

import com.dipsource.dto.feed.FeedCursorResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.feed.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 내가 쓴 피드 목록 조회 — GET /api/v1/users/me/feeds */
@RestController
@RequestMapping("/api/v1/users/me/feeds")
@RequiredArgsConstructor
public class MyFeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<ApiResponse<FeedCursorResponse>> getMyFeeds(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        FeedCursorResponse response = feedService.getMyFeeds(principal.getUserId(), cursor, size);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
