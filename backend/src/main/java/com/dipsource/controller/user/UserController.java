package com.dipsource.controller.user;

import com.dipsource.dto.user.CompleteProfileRequest;
import com.dipsource.dto.user.NicknameAvailableResponse;
import com.dipsource.dto.user.UserMeResponse;
import com.dipsource.dto.user.UserProfileResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 회원 조회·온보딩 프로필 API. */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /** 내 정보 조회 */
    @GetMapping("/me")
    public ApiResponse<UserMeResponse> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.of(userService.getMe(principal.getUserId()));
    }

    /** 닉네임 사용 가능 여부 */
    @GetMapping("/nicknames/available")
    public ApiResponse<NicknameAvailableResponse> checkNickname(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @NotBlank String nickname
    ) {
        return ApiResponse.of(userService.isNicknameAvailable(principal.getUserId(), nickname));
    }

    /** 온보딩 프로필 저장 */
    @PostMapping("/me/profile")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserMeResponse> completeProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CompleteProfileRequest request
    ) {
        return ApiResponse.of(userService.completeProfile(principal.getUserId(), request));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getProfile(
        @PathVariable Long userId,
        @AuthenticationPrincipal UserPrincipal principal) {
            Long viewerId = principal == null ? null : principal.getUserId();
            return ApiResponse.of(userService.getProfile(userId, viewerId));
    }
}
