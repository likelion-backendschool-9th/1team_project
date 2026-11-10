package com.dipsource.controller.feed;

import com.dipsource.dto.feed.FeedCursorResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.feed.FeedRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
public class FeedRecommendationController {
    private static final int MAX_SIZE = 100;
    private final FeedRecommendationService recommendationService;

    /**
     * 피드 조회.
     * - type=recommended: 점수 기반 + 트렌딩 (로그인 필요)
     * - type=following: 팔로잉한 사람 글만 (로그인 필요)
     * - 비로그인: 트렌딩 + 최신
     * - tag가 있으면: 그 태그가 달린 글만 최신순
     * <p>
     * 대상 사용자는 쿼리 userId가 아니라 JWT(쿠키)의 {@link UserPrincipal}에서만 결정한다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<FeedCursorResponse>> getFeeds(
            @AuthenticationPrincipal UserPrincipal principal,
            // type: 피드 종류. "recommended"(추천), "following"(팔로잉). 기본값 recommended
            @RequestParam(defaultValue = "recommended") String type,
            // size: 한 번에 가져올 피드 수. 기본값 20
            @RequestParam(defaultValue = "20") int size,
            // cursor: 마지막으로 본 피드 ID. 무한스크롤 시 이전 응답의 nextCursor 값을 넘김. 첫 요청이면 안 보냄
            @RequestParam(required = false) Long cursor,
            // tag: 태그 이름. 있으면 type과 관계없이 그 태그가 달린 글만 최신순으로 (비로그인 가능)
            @RequestParam(required = false) String tag) {
        Long userId = principal == null ? null : principal.getUserId();
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        FeedCursorResponse response;

        if (tag != null && !tag.isBlank()) {
            response = recommendationService.getTagFeeds(userId, tag.strip(), cursor, safeSize);
        } else if (userId == null) {
            response = recommendationService.getGuestFeeds(cursor, safeSize);
        } else if ("following".equals(type)) {
            response = recommendationService.getFollowingFeeds(userId, cursor, safeSize);
        } else if ("recommended".equals(type)) {
            response = recommendationService.getRecommendedFeeds(userId, cursor, safeSize);
        } else if ("latest".equals(type)) {
            response = recommendationService.getLatestFeeds(userId, cursor, safeSize);
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "지원하지 않는 피드 타입입니다: " + type);
        }

        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
