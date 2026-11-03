package com.dipsource.controller.feed;

import com.dipsource.dto.feed.FeedCreateRequest;
import com.dipsource.dto.feed.FeedResponse;
import com.dipsource.dto.feed.FeedUpdateRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.feed.FeedService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 피드 CRUD 컨트롤러
 * 추천/트렌딩/팔로잉 피드 목록 조회는 FeedRecommendationController가 담당함
 *
 * POST   /api/v1/feeds           — 피드 생성 (인증 필요)
 * GET    /api/v1/feeds/{feedId}  — 피드 단건 조회 (비로그인 가능)
 * PUT    /api/v1/feeds/{feedId}  — 피드 수정 (인증 필요, 본인만)
 * DELETE /api/v1/feeds/{feedId}  — 피드 삭제 (인증 필요, 본인만)
 */
@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /** 피드 생성. JWT 인증된 사용자만 호출 가능. 201 Created 반환 */
    @PostMapping
    public ResponseEntity<ApiResponse<FeedResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FeedCreateRequest request
    ) {
        FeedResponse response = feedService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 피드 단건 조회. 비로그인도 가능 (SecurityConfig에서 GET /api/v1/feeds/* permitAll) */
    @GetMapping("/{feedId}")
    public ResponseEntity<ApiResponse<FeedResponse>> getById(@PathVariable Long feedId) {
        FeedResponse response = feedService.getById(feedId);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /** 피드 수정. 본인이 작성한 피드만 수정 가능 */
    @PutMapping("/{feedId}")
    public ResponseEntity<ApiResponse<FeedResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long feedId,
            @Valid @RequestBody FeedUpdateRequest request
    ) {
        FeedResponse response = feedService.update(principal.getUserId(), feedId, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /** 피드 삭제 (소프트 삭제). 본인이 작성한 피드만 삭제 가능. 204 No Content 반환 */
    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long feedId
    ) {
        feedService.delete(principal.getUserId(), feedId);
        return ResponseEntity.noContent().build();
    }
}
