package com.dipsource.global.auth;

import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.repository.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** 요청 쿠키의 Access Token을 읽어 SecurityContext에 로그인 정보를 넣는다. */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = readCookie(request, AuthCookieNames.ACCESS);

        // 유효한 Access Token이 있으면 인증 정보를 세팅한다.
        if (accessToken != null && jwtProvider.isAccessToken(accessToken)) {
            Long userId = jwtProvider.getUserId(accessToken);
            boolean active = userRepository.findById(userId)
                    .map(user -> user.getStatus() == UserStatus.ACTIVE)
                    .orElse(false);
            if (active) {
                String role = jwtProvider.getRole(accessToken);
                UserPrincipal principal = new UserPrincipal(userId, role == null ? "USER" : role);
                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
