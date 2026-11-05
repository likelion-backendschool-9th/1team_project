package com.dipsource.controller.follow;

import com.dipsource.dto.follow.FollowPresenceResponse;
import com.dipsource.dto.follow.FollowUserResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.follow.FollowService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 팔로우·언팔로우와 팔로잉·팔로워 목록. "나"는 JWT에서만 가져온다. */
@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /** 팔로우. 이미 팔로우 중이어도 204 (멱등). */
    @PostMapping("/{userId}")
    public ResponseEntity<Void> follow(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId
    ) {
        followService.follow(requireUserId(principal), userId);
        return ResponseEntity.noContent().build();
    }

    /** 언팔로우. 팔로우 중이 아니어도 204 (멱등). */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unfollow(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId
    ) {
        followService.unfollow(requireUserId(principal), userId);
        return ResponseEntity.noContent().build();
    }

    /** 내가 팔로우하는 회원 목록. 최근에 팔로우한 순. */
    @GetMapping("/followings")
    public ResponseEntity<ApiResponse<PageResponse<FollowUserResponse>>> followings(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of(followService.getFollowings(requireUserId(principal), pageable)));
    }

    /** 나를 팔로우하는 회원 목록. 최근에 팔로우한 순. */
    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<PageResponse<FollowUserResponse>>> followers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of(followService.getFollowers(requireUserId(principal), pageable)));
    }

    /** 화면에 나온 팔로잉 회원의 접속 상태. ?userIds=1,2,3 (최대 100개) */
    @GetMapping("/followings/presence")
    public ResponseEntity<ApiResponse<List<FollowPresenceResponse>>> followingPresence(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) List<Long> userIds
    ) {
        return ResponseEntity.ok(ApiResponse.of(followService.getFollowingPresence(requireUserId(principal), userIds)));
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
