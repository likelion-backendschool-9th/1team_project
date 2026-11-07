package com.dipsource.controller.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.report.Report;
import com.dipsource.entity.user.User;
import com.dipsource.global.auth.UserPrincipal;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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
class AdminFeedControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;

    User admin;
    User author;
    User reporter1;
    User reporter2;
    Feed plain;
    Feed percent;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE reports, admin_action_logs, feeds, users RESTART IDENTITY CASCADE");
        admin = persist(User.builder().email("admin@test.local").nickname("관리자").techStacks(List.of()).build());
        author = persist(User.builder().email("author@test.local").nickname("검색러").techStacks(List.of()).build());
        reporter1 = persist(User.builder().email("r1@test.local").nickname("신고1").techStacks(List.of()).build());
        reporter2 = persist(User.builder().email("r2@test.local").nickname("신고2").techStacks(List.of()).build());
        plain = persist(Feed.builder().userId(author.getId()).content("일반글").build());
        percent = persist(Feed.builder().userId(author.getId()).content("100% 완료").build());
        persist(Report.builder().reporterId(reporter1.getId()).targetType("FEED").targetId(percent.getId()).reason("스팸").build());
        persist(Report.builder().reporterId(reporter2.getId()).targetType("FEED").targetId(percent.getId()).reason("광고").build());
    }

    @Test
    @DisplayName("일반 회원은 관리자 피드 목록이 403")
    void userForbidden() throws Exception {
        mockMvc.perform(get("/admin/feeds").with(authentication(auth(author, "USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("관리자는 200, 닉네임·신고 수·% 검색 이스케이프")
    void adminListMapsAndEscapes() throws Exception {
        mockMvc.perform(get("/admin/feeds")
                        .param("q", "%")
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("100% 완료"))
                .andExpect(jsonPath("$.data.content[0].author").value("검색러"))
                .andExpect(jsonPath("$.data.content[0].reported").value(2))
                .andExpect(jsonPath("$.data.content[0].metric").value("좋아요 0"))
                .andExpect(jsonPath("$.data.content[0].status").value("visible"));
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