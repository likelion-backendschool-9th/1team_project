package com.dipsource.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class MasterRoleControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;

    User master;
    User admin;
    User member;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE admin_action_logs, users RESTART IDENTITY CASCADE");
        master = persist(User.builder().email("master@test.local").nickname("마스터").techStacks(List.of()).build());
        admin = persist(User.builder().email("admin@test.local").nickname("관리자").techStacks(List.of()).build());
        member = persist(User.builder().email("member@test.local").nickname("회원").techStacks(List.of()).build());
        jdbc.update("UPDATE users SET role = 'MASTER' WHERE id = ?", master.getId());
        jdbc.update("UPDATE users SET role = 'ADMIN' WHERE id = ?", admin.getId());
    }

    @Test
    @DisplayName("일반 관리자는 권한 관리 API에 접근할 수 없다 (403)")
    void adminForbidden() throws Exception {
        mockMvc.perform(get("/admin/roles/users").with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/admin/roles/users/" + member.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}")
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("마스터는 기존 관리자 API도 그대로 쓸 수 있다")
    void masterCanUseAdminApi() throws Exception {
        mockMvc.perform(get("/admin/users").with(authentication(auth(master, "MASTER"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("마스터는 회원 목록을 마스터·관리자 먼저 받는다")
    void masterList() throws Exception {
        mockMvc.perform(get("/admin/roles/users").with(authentication(auth(master, "MASTER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].role").value("MASTER"))
                .andExpect(jsonPath("$.data.content[1].role").value("ADMIN"))
                .andExpect(jsonPath("$.data.content[2].role").value("USER"));

        mockMvc.perform(get("/admin/roles/users").param("role", "USER")
                        .with(authentication(auth(master, "MASTER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].nickname").value("회원"));
    }

    @Test
    @DisplayName("마스터는 회원을 관리자로 지정하고 다시 일반 회원으로 내릴 수 있다")
    void grantAndRevoke() throws Exception {
        mockMvc.perform(put("/admin/roles/users/" + member.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}")
                        .with(authentication(auth(master, "MASTER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
        assertThat(roleOf(member)).isEqualTo("ADMIN");

        mockMvc.perform(put("/admin/roles/users/" + member.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\"}")
                        .with(authentication(auth(master, "MASTER"))))
                .andExpect(status().isOk());
        assertThat(roleOf(member)).isEqualTo("USER");

        Integer logs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM admin_action_logs WHERE target_type = 'USER' AND target_id = ?",
                Integer.class, member.getId());
        assertThat(logs).isEqualTo(2);
    }

    @Test
    @DisplayName("마스터 권한은 줄 수 없고, 자기 자신과 다른 마스터의 권한은 바꿀 수 없다")
    void guards() throws Exception {
        mockMvc.perform(put("/admin/roles/users/" + member.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"MASTER\"}")
                        .with(authentication(auth(master, "MASTER"))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/admin/roles/users/" + master.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\"}")
                        .with(authentication(auth(master, "MASTER"))))
                .andExpect(status().isBadRequest());
        assertThat(roleOf(master)).isEqualTo("MASTER");
    }

    private String roleOf(User user) {
        return jdbc.queryForObject("SELECT role FROM users WHERE id = ?", String.class, user.getId());
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
