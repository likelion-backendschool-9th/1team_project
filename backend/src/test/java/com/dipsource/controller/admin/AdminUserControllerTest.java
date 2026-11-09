package com.dipsource.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoType;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostCategory;
import com.dipsource.entity.report.Report;
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
class AdminUserControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository userRepository;
    @Autowired JwtProvider jwtProvider;

    User admin;
    User member;
    User reporter;

    @BeforeEach
    void setUp() {
        jdbc.execute("""
                TRUNCATE reports, user_sanctions, posts, demos, users
                RESTART IDENTITY CASCADE
                """);
        admin = persist(User.builder().email("admin@test.local").nickname("관리자").techStacks(List.of()).build());
        member = persist(User.builder().email("member@test.local").nickname("회원").techStacks(List.of()).build());
        reporter = persist(User.builder().email("r@test.local").nickname("신고자").techStacks(List.of()).build());
        persist(Demo.builder().userId(member.getId()).title("데모").description("설명").demoType(DemoType.STATIC).build());
        persist(Post.builder().userId(member.getId()).title("글").category(PostCategory.BACKEND).content("본문").build());
        persist(Report.builder()
                .reporterId(reporter.getId())
                .targetType("USER")
                .targetId(member.getId())
                .reason("스팸 계정")
                .build());
    }

    @Test
    @DisplayName("미인증은 관리자 회원 목록이 401")
    void unauthorized() throws Exception {
        mockMvc.perform(get("/admin/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("일반 회원은 관리자 회원 목록이 403")
    void userForbidden() throws Exception {
        mockMvc.perform(get("/admin/users").with(authentication(auth(member, "USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("관리자는 회원 목록을 보고 데모·글·신고 수를 받는다")
    void adminList() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("q", "회원")
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].nickname").value("회원"))
                .andExpect(jsonPath("$.data.content[0].demoCount").value(1))
                .andExpect(jsonPath("$.data.content[0].postCount").value(1))
                .andExpect(jsonPath("$.data.content[0].reportCount").value(1));
    }

    @Test
    @DisplayName("제재하면 상태가 SUSPENDED가 되고 기존 토큰으로는 인증되지 않는다")
    void banStopsJwt() throws Exception {
        mockMvc.perform(put("/admin/users/{id}/ban", member.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"period":"1일","reason":"스팸 반복입니다"}
                                """)
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isNoContent());

        em.clear();
        assertThat(userRepository.findById(member.getId()).orElseThrow().getStatus())
                .isEqualTo(UserStatus.SUSPENDED);

        String token = jwtProvider.createAccessToken(member.getId(), "USER");
        mockMvc.perform(get("/users/me").cookie(new Cookie(AuthCookieNames.ACCESS, token)))
                .andExpect(status().isUnauthorized());
    }

    private Authentication auth(User user, String role) {
        UserPrincipal principal = new UserPrincipal(user.getId(), role);
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
