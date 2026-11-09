package com.dipsource.controller.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoType;
import com.dipsource.entity.report.Report;
import com.dipsource.entity.user.User;
import com.dipsource.global.auth.UserPrincipal;
import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
class AdminDashboardControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;

    User admin;
    User member;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE reports, demos, users RESTART IDENTITY CASCADE");
        admin = persist(User.builder().email("admin@test.local").nickname("관리자").techStacks(List.of()).build());
        member = persist(User.builder().email("member@test.local").nickname("회원").techStacks(List.of()).build());
        persist(Demo.builder()
                .userId(member.getId())
                .title("이번 주 데모")
                .description("설명")
                .demoType(DemoType.STATIC)
                .build());
        persist(Report.builder()
                .reporterId(admin.getId())
                .targetType("USER")
                .targetId(member.getId())
                .reason("스팸 계정")
                .build());
        persist(Report.builder()
                .reporterId(member.getId())
                .targetType("USER")
                .targetId(admin.getId())
                .reason("이미 처리된 신고")
                .build());
        persist(User.builder().email("gone@test.local").nickname("탈퇴회원").techStacks(List.of()).build());
        jdbc.update("UPDATE reports SET status = 'RESOLVED' WHERE reason = '이미 처리된 신고'");
        jdbc.update("UPDATE users SET status = 'DELETED' WHERE email = 'gone@test.local'");
    }

    @Test
    @DisplayName("미인증은 대시보드가 401")
    void unauthorized() throws Exception {
        mockMvc.perform(get("/admin/dashboard")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("일반 회원은 대시보드가 403")
    void userForbidden() throws Exception {
        mockMvc.perform(get("/admin/dashboard").with(authentication(auth(member, "USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("관리자는 회원·데모·대기 신고 수를 받고 매출은 0이다")
    void adminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard").with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userCount").value(2))
                .andExpect(jsonPath("$.data.newUsersToday").value(2))
                .andExpect(jsonPath("$.data.demoUploadsWeek").value(1))
                .andExpect(jsonPath("$.data.pendingReports").value(1))
                .andExpect(jsonPath("$.data.revenueMonth").value(0))
                .andExpect(jsonPath("$.data.signups.length()").value(7));
    }

    @Test
    @DisplayName("서울 자정 직전 가입은 오늘 가입에 넣지 않는다")
    void seoulMidnightBoundary() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime beforeMidnight = today.atStartOfDay().minusSeconds(1);
        LocalDateTime atMidnight = today.atStartOfDay();
        User before = persist(User.builder()
                .email("before-midnight@test.local")
                .nickname("어제가입")
                .techStacks(List.of())
                .build());
        User after = persist(User.builder()
                .email("after-midnight@test.local")
                .nickname("오늘가입")
                .techStacks(List.of())
                .build());
        jdbc.update("UPDATE users SET created_at = ? WHERE id = ?", Timestamp.valueOf(beforeMidnight), before.getId());
        jdbc.update("UPDATE users SET created_at = ? WHERE id = ?", Timestamp.valueOf(atMidnight), after.getId());

        int todayIndex = today.getDayOfWeek().getValue() - 1;
        mockMvc.perform(get("/admin/dashboard").with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userCount").value(4))
                .andExpect(jsonPath("$.data.newUsersToday").value(3))
                .andExpect(jsonPath("$.data.signups[" + todayIndex + "]").value(3));
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
