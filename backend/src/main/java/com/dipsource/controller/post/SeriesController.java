package com.dipsource.controller.post;

import com.dipsource.dto.post.MySeriesPurchaseResponse;
import com.dipsource.dto.post.SeriesDetailResponse;
import com.dipsource.dto.post.SeriesIdResponse;
import com.dipsource.dto.post.SeriesSummaryResponse;
import com.dipsource.dto.post.SeriesWriteRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.post.SeriesService;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 블로그 연재 시리즈. 조회는 비로그인도 가능하고(SecurityConfig), 나머지는 로그인이 필요하다.
 * 시리즈는 현금으로 한 번 사면 이후 회차까지 계속 읽는다.
 */
@RestController
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    /** 인기 시리즈 (구매 많은 순, 최대 20개) */
    @GetMapping("/series")
    public ApiResponse<List<SeriesSummaryResponse>> popular(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.of(seriesService.getPopular(viewerId(principal), size));
    }

    /** 시리즈 화면 (정보·회차 목록·구매 여부) */
    @GetMapping("/series/{seriesId}")
    public ApiResponse<SeriesDetailResponse> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long seriesId
    ) {
        return ApiResponse.of(seriesService.getSeries(seriesId, viewerId(principal)));
    }

    /** 작가의 시리즈 목록 */
    @GetMapping("/users/{userId}/series")
    public ApiResponse<List<SeriesSummaryResponse>> userSeries(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId
    ) {
        return ApiResponse.of(seriesService.getUserSeries(userId, viewerId(principal)));
    }

    /** 내가 산 시리즈 */
    @GetMapping("/users/me/series-purchases")
    public ApiResponse<List<MySeriesPurchaseResponse>> myPurchases(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.of(seriesService.getMyPurchases(requireUserId(principal)));
    }

    /** 시리즈 만들기 */
    @PostMapping("/series")
    public ResponseEntity<ApiResponse<SeriesIdResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SeriesWriteRequest request
    ) {
        SeriesIdResponse response = seriesService.create(requireUserId(principal), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 시리즈 수정 (본인만) */
    @PutMapping("/series/{seriesId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long seriesId,
            @Valid @RequestBody SeriesWriteRequest request
    ) {
        seriesService.update(requireUserId(principal), seriesId, request);
        return ResponseEntity.noContent().build();
    }

    /** 시리즈 삭제 (본인만, 회차·구매자가 없을 때) */
    @DeleteMapping("/series/{seriesId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long seriesId
    ) {
        seriesService.delete(requireUserId(principal), seriesId);
        return ResponseEntity.noContent().build();
    }

    /** 시리즈 구매 (현금). 이미 샀으면 204 (멱등) */
    @PostMapping("/series/{seriesId}/purchase")
    public ResponseEntity<Void> purchase(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long seriesId
    ) {
        seriesService.purchase(requireUserId(principal), seriesId);
        return ResponseEntity.noContent().build();
    }

    private static Long viewerId(UserPrincipal principal) {
        return principal == null ? null : principal.getUserId();
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
