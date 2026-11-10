package com.dipsource.controller.user;

import com.dipsource.entity.user.enums.SocialProvider;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.user.AuthService;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 내 소셜 계정 연동 목록·해제 — GET /users/me/social-accounts, DELETE /users/me/social-accounts/{provider} */
@RestController
@RequestMapping("/users/me/social-accounts")
@RequiredArgsConstructor
public class SocialAccountController {

    private final AuthService authService;

    /** 연결된 소셜 목록. 예: ["github", "kakao"] */
    @GetMapping
    public ApiResponse<List<String>> list(@AuthenticationPrincipal UserPrincipal principal) {
        List<String> providers = authService.getLinkedProviders(principal.getUserId()).stream()
                .map(provider -> provider.name().toLowerCase(Locale.ROOT))
                .toList();
        return ApiResponse.of(providers);
    }

    /** 연동 해제. 마지막 하나는 해제할 수 없다 */
    @DeleteMapping("/{provider}")
    public ResponseEntity<Void> unlink(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String provider) {
        authService.unlinkSocialAccount(principal.getUserId(), parse(provider));
        return ResponseEntity.noContent().build();
    }

    private static SocialProvider parse(String provider) {
        try {
            return SocialProvider.valueOf(provider.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "지원하지 않는 소셜 로그인이에요.");
        }
    }
}