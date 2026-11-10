package com.dipsource.controller.user;

import com.dipsource.global.auth.AuthCookieNames;
import com.dipsource.global.auth.AuthCookieWriter;
import com.dipsource.global.auth.OAuth2SuccessHandler;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.user.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/** 소셜 로그인 시작·로그아웃∙재발급 API. */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCookieWriter authCookieWriter;
    private final AuthService authService;

    /**
     * 소셜 로그인을 시작한다.
     * 예: GET /auth/login/github → GitHub 인가 화면으로 이동
     */
    @GetMapping("/login/{provider}")
    public void login(@PathVariable String provider, HttpServletRequest request ,HttpServletResponse response) throws IOException {
        // Spring Security OAuth2 기본 시작 URL로 보낸다.
        response.sendRedirect(request.getContextPath() + "/oauth2/authorization/" + provider);
    }

    /** 로그아웃: JWT 쿠키를 지운다. */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
        @CookieValue(name = AuthCookieNames.REFRESH, required = false) String refreshToken, 
        HttpServletResponse response) {
        authService.revokePresented(refreshToken);
        authCookieWriter.clear(response);
        return ResponseEntity.ok(Map.of("data", Map.of("loggedOut", true)));
    }


    /**
         * Access Token 재발급. refresh_token 쿠키로 새 Access·Refresh를 발급한다.
         * 실패하면 남은 인증 쿠키를 지워 로그아웃 상태로 만든다.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
        @CookieValue(name = AuthCookieNames.REFRESH, required = false) String refreshToken,
        HttpServletResponse response
    ) {
        try {
            AuthService.IssuedTokens tokens = authService.rotate(refreshToken);
            authCookieWriter.writeTokens(response, tokens.accessToken(), tokens.refreshToken());
            return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
            authCookieWriter.clear(response);
            throw e;
        }
        
    }

    /**
     * 소셜 계정 연동을 시작한다. 로그인한 상태여야 한다.
     * 예: GET /auth/link/kakao → 카카오 로그인 → 지금 회원에 카카오 계정이 연결된다
     */
    @GetMapping("/link/{provider}")
    public void link(
            @PathVariable String provider,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        request.getSession(true).setAttribute(OAuth2SuccessHandler.LINK_USER_ID, principal.getUserId());
        response.sendRedirect(request.getContextPath() + "/oauth2/authorization/" + provider);
    }
    
}
