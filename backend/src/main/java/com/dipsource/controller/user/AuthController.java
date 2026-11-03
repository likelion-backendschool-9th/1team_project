package com.dipsource.controller.user;

import com.dipsource.global.auth.AuthCookieWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 소셜 로그인 시작·로그아웃 API. */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCookieWriter authCookieWriter;

    /**
     * 소셜 로그인을 시작한다.
     * 예: GET /api/v1/auth/login/github → GitHub 인가 화면으로 이동
     */
    @GetMapping("/login/{provider}")
    public void login(@PathVariable String provider, HttpServletResponse response) throws IOException {
        // Spring Security OAuth2 기본 시작 URL로 보낸다.
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    /** 로그아웃: JWT 쿠키를 지운다. */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        authCookieWriter.clear(response);
        return ResponseEntity.ok(Map.of("data", Map.of("loggedOut", true)));
    }
}
