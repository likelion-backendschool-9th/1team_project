package com.dipsource.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.auth.AuthCookieNames;
import com.dipsource.global.auth.JwtProvider;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test"
})
class UserControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository userRepository;
    @Autowired JwtProvider jwtProvider;

    User me;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE users RESTART IDENTITY CASCADE");
        me = persist(User.builder()
                .email("me@test.local")
                .nickname("원래닉")
                .techStacks(List.of("java", "spring"))
                .build());
    }

    @Test
    @DisplayName("비로그인은 내 정보 수정·탈퇴가 401")
    void unauthorized() throws Exception {
        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"새닉","bio":"소개","techStacks":"java,spring"}
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("내 정보를 수정하면 닉네임과 소개가 바뀐다")
    void updateMe() throws Exception {
        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"새닉","bio":"소개글","techStacks":"java,react"}
                                """)
                        .with(authentication(auth(me))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("새닉"))
                .andExpect(jsonPath("$.data.bio").value("소개글"));
    }

    @Test
    @DisplayName("탈퇴하면 상태가 DELETED가 되고 기존 토큰으로는 인증되지 않는다")
    void withdrawStopsJwt() throws Exception {
        mockMvc.perform(delete("/users/me").with(authentication(auth(me))))
                .andExpect(status().isNoContent());

        em.clear();
        assertThat(userRepository.findById(me.getId()).orElseThrow().getStatus())
                .isEqualTo(UserStatus.DELETED);

        String token = jwtProvider.createAccessToken(me.getId(), "USER");
        mockMvc.perform(get("/users/me").cookie(new Cookie(AuthCookieNames.ACCESS, token)))
                .andExpect(status().isUnauthorized());
    }

    private Authentication auth(User user) {
        UserPrincipal principal = new UserPrincipal(user.getId(), "USER");
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private <T> T persist(T entity) {
        return tx.execute(status -> {
            em.persist(entity);
            em.flush();
            return entity;
        });
    }
}
