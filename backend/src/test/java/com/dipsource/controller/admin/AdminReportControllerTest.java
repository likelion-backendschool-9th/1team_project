package com.dipsource.controller.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class AdminReportControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;

    User admin;
    User reporter;
    User author;
    Report pending;
    Report resolved;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE reports, feeds, users RESTART IDENTITY CASCADE");
        admin = persist(User.builder().email("admin@test.local").nickname("관리자").techStacks(List.of()).build());
        reporter = persist(User.builder().email("reporter@test.local").nickname("신고자").techStacks(List.of()).build());
        author = persist(User.builder().email("author@test.local").nickname("작성자").techStacks(List.of()).build());
        Feed feed = persist(Feed.builder().userId(author.getId()).content("스팸 링크 피드").build());
        Feed other = persist(Feed.builder().userId(author.getId()).content("이미 처리된 피드").build());
        pending = persist(Report.builder()
                .reporterId(reporter.getId())
                .targetType("FEED")
                .targetId(feed.getId())
                .reason("스팸·광고")
                .contentSnapshot("스팸 링크 피드")
                .build());
        resolved = persist(Report.builder()
                .reporterId(reporter.getId())
                .targetType("FEED")
                .targetId(other.getId())
                .reason("이미 처리")
                .contentSnapshot("이미 처리된 피드")
                .build());
        tx.executeWithoutResult(status -> {
            Report attached = em.find(Report.class, resolved.getId());
            attached.resolve(admin.getId(), "숨김 처리");
        });
    }

    @Test
    @DisplayName("일반 회원은 관리자 신고 목록이 403")
    void userForbidden() throws Exception {
        mockMvc.perform(get("/admin/reports").with(authentication(auth(author, "USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("관리자는 대기 신고를 앞에 두고 닉네임·한글 유형을 받는다")
    void adminListPendingFirst() throws Exception {
        mockMvc.perform(get("/admin/reports").with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].id").value(pending.getId().intValue()))
                .andExpect(jsonPath("$.data.content[0].targetType").value("피드"))
                .andExpect(jsonPath("$.data.content[0].targetTitle").value("스팸 링크 피드"))
                .andExpect(jsonPath("$.data.content[0].reporter").value("신고자"))
                .andExpect(jsonPath("$.data.content[0].reported").value("작성자"))
                .andExpect(jsonPath("$.data.content[0].status").value("pending"))
                .andExpect(jsonPath("$.data.content[1].status").value("resolved"))
                .andExpect(jsonPath("$.data.content[1].handledBy").value("관리자"))
                .andExpect(jsonPath("$.data.content[1].adminNote").value("숨김 처리"));
    }

    @Test
    @DisplayName("type·status 필터")
    void adminListFilters() throws Exception {
        mockMvc.perform(get("/admin/reports")
                        .param("type", "feeds")
                        .param("status", "pending")
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(pending.getId().intValue()));
    }

    @Test
    @DisplayName("회원 신고 유형으로 필터한다")
    void adminListUserType() throws Exception {
        persist(Report.builder()
                .reporterId(reporter.getId())
                .targetType("USER")
                .targetId(author.getId())
                .reason("스팸 계정")
                .build());

        mockMvc.perform(get("/admin/reports")
                        .param("type", "users")
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].targetType").value("회원"))
                .andExpect(jsonPath("$.data.content[0].reported").value("작성자"));
    }

    @Test
    @DisplayName("기각하면 REJECTED가 되고 재처리하면 409")
    void rejectThenConflict() throws Exception {
        mockMvc.perform(put("/admin/reports/{id}", pending.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"reject\",\"memo\":\"해당 없음입니다\"}")
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/admin/reports/{id}", pending.getId())
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("rejected"))
                .andExpect(jsonPath("$.data.handledBy").value("관리자"))
                .andExpect(jsonPath("$.data.adminNote").value("해당 없음입니다"));

        mockMvc.perform(put("/admin/reports/{id}", pending.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"hide\",\"memo\":\"다시 처리합니다\"}")
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isConflict());
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
