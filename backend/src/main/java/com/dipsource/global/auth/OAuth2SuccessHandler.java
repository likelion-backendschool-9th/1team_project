package com.dipsource.global.auth;

import com.dipsource.service.user.AuthResult;
import com.dipsource.service.user.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/** GitHub OAuth 로그인 성공 후 JWT 쿠키를 심고 프론트로 보낸다. */
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
            Authentication authentication
    ) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauthUser.getAttributes();

        // GitHub 프로필에서 필요한 값만 꺼낸다.
        String providerId = String.valueOf(attributes.get("id"));
        String email = (String) attributes.get("email");
        String login = (String) attributes.get("login");
        String avatarUrl = (String) attributes.get("avatar_url");

        AuthResult result = authService.findOrCreateFromGithub(providerId, email, login, avatarUrl);

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
