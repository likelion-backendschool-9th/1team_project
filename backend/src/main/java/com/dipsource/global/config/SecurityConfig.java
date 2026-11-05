package com.dipsource.global.config;

import com.dipsource.global.auth.JwtAuthenticationFilter;
import com.dipsource.global.auth.OAuth2SuccessHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Spring Security · OAuth2 · JWT 필터 설정. */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST + JWT라 CSRF는 끈다.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                // OAuth 인가 state 저장을 위해 세션은 필요할 때만 만든다.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // 로그인·OAuth 콜백은 비로그인 허용
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                        // 조회성 API는 비로그인도 가능
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/tags",
                                "/api/v1/demos", "/api/v1/demos/*",
                                "/api/v1/feeds", "/api/v1/feeds/*", "/api/v1/feeds/*/comments", "/api/v1/feeds/comments/*/replies",
                                "/api/v1/posts", "/api/v1/posts/list", "/api/v1/posts/*", "/api/v1/posts/*/comments",
                                "/api/v1/users/*/posts",
                                "/api/v1/search", "/api/v1/search/**",
                                "/api/v1/events", "/api/v1/events/*",
                                "/api/v1/rankings",
                                "/api/v1/notifications", "/api/v1/notifications/**",
                                "/api/v1/follows/**",
                                "/api/v1/coins/**",
                                "/api/v1/users/me/bookmarks", "/api/v1/users/*").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // 그 외 API는 인증 필요 (이후 공개 API는 여기서 풀면 된다)
                        .anyRequest().authenticated()
                )
                // /api/** 요청이 인증 실패하면 OAuth 리다이렉트 대신 401 JSON 반환
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                apiAuthenticationEntryPoint(),
                                request -> request.getServletPath().startsWith("/api/")
                        )
                )
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2SuccessHandler)
                )
                // 매 요청마다 JWT 쿠키를 검사한다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** API 요청에 인증이 없으면 OAuth 리다이렉트 대신 401을 반환한다. */
    @Bean
    public AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\"}");
        };
    }

    /** 프론트 주소(app.frontend-base-url, 기본 localhost:3000)에서 API·WebSocket을 호출할 수 있게 CORS를 연다. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.frontend-base-url}") String frontendBaseUrl) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendBaseUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
