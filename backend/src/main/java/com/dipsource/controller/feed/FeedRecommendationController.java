package com.dipsource.controller.feed;

import com.dipsource.dto.feed.FeedCursorResponse;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.feed.FeedRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
public class FeedRecommendationController {

    private final FeedRecommendationService recommendationService;

    /**
     * 피드 조회.
     * - type=recommended: 점수 기반 + 트렌딩 (로그인 필요)
     * - type=following: 팔로잉한 사람 글만 (로그인 필요)
     * - type=trending: 트렌딩만
     * - 비로그인: 트렌딩 + 최신
     */
    @GetMapping
    public ResponseEntity<ApiResponse<FeedCursorResponse>> getFeeds(
            // type: 피드 종류. "recommended"(추천), "following"(팔로잉). 기본값 recommended
            @RequestParam(defaultValue = "recommended") String type,
            // size: 한 번에 가져올 피드 수. 기본값 20
            @RequestParam(defaultValue = "20") int size,
            // cursor: 마지막으로 본 피드 ID. 무한스크롤 시 이전 응답의 nextCursor 값을 넘김. 첫 요청이면 안 보냄
            @RequestParam(required = false) Long cursor,
            // userId: 로그인한 사용자 ID. 비로그인이면 안 보냄. TODO: Spring Security 인증으로 변경
            @RequestParam(required = false) Long userId
    ) {
        FeedCursorResponse response;

        if (userId == null) {
            response = recommendationService.getGuestFeeds(cursor, size);
        } else if ("following".equals(type)) {
            response = recommendationService.getFollowingFeeds(userId, cursor, size);
        } else {
            response = recommendationService.getRecommendedFeeds(userId, cursor, size);
        }

        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
