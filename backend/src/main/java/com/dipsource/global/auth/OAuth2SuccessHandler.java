package com.dipsource.global.auth;

import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.exception.BusinessException;
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

    /** 소셜 계정 연동을 시작한 회원 ID를 세션에 잠깐 담아 두는 이름 (AuthController.link에서 넣는다) */
    public static final String LINK_USER_ID = "OAUTH_LINK_USER_ID";
    private final AuthService authService;
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

        // 내 정보에서 "연동하기"로 들어온 경우: 새로 가입·로그인하지 않고 지금 회원에 붙인다
        HttpSession linkSession = request.getSession(false);
        Long linkUserId = linkSession == null ? null : (Long) linkSession.getAttribute(LINK_USER_ID);
        if (linkUserId != null) {
            linkSession.removeAttribute(LINK_USER_ID);
            String query;
            try {
                authService.linkSocialAccount(linkUserId, profile);
                query = "linked=" + profile.provider().name().toLowerCase();
            } catch (BusinessException e) {
                query = "linkError=" + e.getErrorCode().name().toLowerCase();
            }
            getRedirectStrategy().sendRedirect(request, response, frontendBaseUrl + "/my?" + query);
            return;
        }

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
        AuthService.IssuedTokens tokens = authService.issueTokens(result.user());
        authCookieWriter.writeTokens(response, tokens.accessToken(), tokens.refreshToken());

        // 기술 스택이 비어 있으면 온보딩 미완료로 보고 온보딩으로 보낸다.
        String path = result.user().isOnboardingIncomplete() ? "/onboarding" : "/main";
        getRedirectStrategy().sendRedirect(request, response, frontendBaseUrl + path);
    }
}
