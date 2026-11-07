package com.dipsource.global.auth;

import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.service.user.AuthResult;
import com.dipsource.service.user.AuthService;
import com.dipsource.service.user.SocialProfile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/** 소셜 로그인 성공 후 JWT 쿠키를 심고 프론트로 보낸다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final AuthCookieWriter authCookieWriter;

    /** 로그인 후 돌아갈 프론트 주소 */
    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        SocialProfile profile = SocialProfile.of(
                token.getAuthorizedClientRegistrationId(),
                token.getPrincipal().getAttributes());

        AuthResult result;
        try {
            result = authService.findOrCreate(profile);
        } catch (DataIntegrityViolationException e) {
            // 같은 계정으로 동시에 첫 로그인한 경우: 다른 요청이 먼저 가입시켰으니 다시 찾는다.
            log.warn("동시 가입 감지, 재시도: provider={}, providerId={}", profile.provider(), profile.providerId());
            result = authService.findOrCreate(profile);
        }
        UserStatus status = result.user().getStatus();
        if (status != UserStatus.ACTIVE) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            authCookieWriter.clear(response);
            String reason = status.name().toLowerCase();
            getRedirectStrategy().sendRedirect(request, response, frontendBaseUrl + "/login?error=" + reason);
            return;

        }

        // 우리 서비스용 JWT를 발급한다.
        String accessToken = jwtProvider.createAccessToken(
                result.user().getId(), result.user().getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(result.user().getId());
        authCookieWriter.writeTokens(response, accessToken, refreshToken);

        // 기술 스택이 비어 있으면 온보딩 미완료로 보고 온보딩으로 보낸다.
        String path = result.user().isOnboardingIncomplete() ? "/onboarding" : "/main";
        getRedirectStrategy().sendRedirect(request, response, frontendBaseUrl + path);
    }
}
