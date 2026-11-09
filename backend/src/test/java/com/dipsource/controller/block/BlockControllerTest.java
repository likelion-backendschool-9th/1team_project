package com.dipsource.controller.block;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class BlockControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;

    User me;
    User other;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE blocks, follows, users RESTART IDENTITY CASCADE");
        me = persist(User.builder().email("me@test.local").nickname("나").techStacks(List.of()).build());
        other = persist(User.builder().email("other@test.local").nickname("상대").techStacks(List.of()).build());
    }

    @Test
    @DisplayName("비로그인은 차단 목록이 401")
    void listUnauthorized() throws Exception {
        mockMvc.perform(get("/blocks")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("차단하면 목록에 나오고 해제하면 빠진다")
    void blockListUnblock() throws Exception {
        mockMvc.perform(post("/blocks/{id}", other.getId()).with(authentication(auth(me))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/blocks").with(authentication(auth(me))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(other.getId().intValue()))
                .andExpect(jsonPath("$.data[0].nickname").value("상대"));

        mockMvc.perform(delete("/blocks/{id}", other.getId()).with(authentication(auth(me))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/blocks").with(authentication(auth(me))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
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
