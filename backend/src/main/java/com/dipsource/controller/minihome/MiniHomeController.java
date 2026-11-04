package com.dipsource.controller.minihome;

import com.dipsource.dto.minihome.FeaturedDemoRequest;
import com.dipsource.dto.minihome.MiniHomeResponse;
import com.dipsource.dto.minihome.RandomMiniHomeResponse;
import com.dipsource.dto.minihome.UpdateMiniHomeRequest;
import com.dipsource.dto.minihome.VisitorStatsResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.minihome.MiniHomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 미니홈피 조회·수정. */
@RestController
@RequestMapping("/api/v1/mini-homes")
@RequiredArgsConstructor
public class MiniHomeController {

    private final MiniHomeService miniHomeService;

    @GetMapping("/random")
    public ApiResponse<RandomMiniHomeResponse> random(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long exclude
    ) {
        return ApiResponse.of(miniHomeService.pickRandom(principal.getUserId(), exclude));
    }

    @GetMapping("/{userId}/visitors")
    public ApiResponse<VisitorStatsResponse> visitors(@PathVariable Long userId) {
        return ApiResponse.of(miniHomeService.getVisitors(userId));
    }

    @PostMapping("/{userId}/visits")
    public ApiResponse<VisitorStatsResponse> visit(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId
    ) {
        return ApiResponse.of(miniHomeService.recordVisit(principal.getUserId(), userId));
    }

    @GetMapping("/{userId}")
    public ApiResponse<MiniHomeResponse> get(@PathVariable Long userId) {
        return ApiResponse.of(miniHomeService.getOrCreate(userId));
    }

    @PutMapping("/me")
    public ApiResponse<MiniHomeResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateMiniHomeRequest request
    ) {
        return ApiResponse.of(miniHomeService.update(principal.getUserId(), request));
    }

    @PutMapping("/me/featured-demo")
    public ApiResponse<MiniHomeResponse> updateFeaturedDemo(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FeaturedDemoRequest request
    ) {
        return ApiResponse.of(miniHomeService.updateFeaturedDemo(principal.getUserId(), request.demoId()));
    }
}
