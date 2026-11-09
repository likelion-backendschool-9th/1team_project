package com.dipsource.controller.user;

import com.dipsource.dto.user.UpdateUserSettingsRequest;
import com.dipsource.dto.user.UserSettingsResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.user.UserSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 내 설정 — GET·PUT /users/me/settings (알림·공개 범위·DM 허용) */
@RestController
@RequestMapping("/users/me/settings")
@RequiredArgsConstructor
public class UserSettingController {

    private final UserSettingService userSettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserSettingsResponse>> getMine(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(userSettingService.getMine(requireUserId(principal))));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserSettingsResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserSettingsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(userSettingService.update(requireUserId(principal), request)));
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
