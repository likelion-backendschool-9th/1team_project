package com.dipsource.controller.minihome;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

/** 미니홈피 API를 실제 DB(Testcontainers)와 보안 설정까지 포함해 확인한다. */
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test"
})
class MiniHomeControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;

    User owner;
    User friend;
    User stranger;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE guestbooks, mini_home_comments, mini_homes, follows, users RESTART IDENTITY CASCADE");
        owner = persist(User.builder().email("owner@test.local").nickname("주인").techStacks(List.of()).build());
        friend = persist(User.builder().email("friend@test.local").nickname("일촌").techStacks(List.of()).build());
        stranger = persist(User.builder().email("stranger@test.local").nickname("손님").techStacks(List.of()).build());
        follow(owner, friend);
        follow(friend, owner);
    }

    @Test
    @DisplayName("비로그인은 미니홈피 조회·수정이 401")
    void anonymousIsUnauthorized() throws Exception {
        mockMvc.perform(get("/mini-homes/{id}", owner.getId())).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/mini-homes/me").contentType(MediaType.APPLICATION_JSON).content(body("cream", "", "")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("홈이 없는 회원을 조회하면 기본값을 주고 행은 만들지 않는다")
    void getDoesNotCreateHome() throws Exception {
        mockMvc.perform(get("/mini-homes/{id}", owner.getId()).with(authentication(auth(stranger))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.headline").value("주인"))
                .andExpect(jsonPath("$.data.skin").value("cream"));

        assertThat(homeCount()).isZero();
    }

    @Test
    @DisplayName("수정하면 저장되고, 다시 조회하면 정리된 값이 나온다")
    void updateAndRead() throws Exception {
        mockMvc.perform(put("/mini-homes/me").with(authentication(auth(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("forest", "rainy-commit", "https://My.Site.dev/")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/mini-homes/{id}", owner.getId()).with(authentication(auth(stranger))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.headline").value("새 소개"))
                .andExpect(jsonPath("$.data.skin").value("forest"))
                .andExpect(jsonPath("$.data.bgm").value("rainy-commit"))
                .andExpect(jsonPath("$.data.domain").value("my.site.dev"));
        assertThat(homeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("목록에 없는 스킨·BGM, 잘못된 도메인, 빈 소개는 400")
    void rejectsInvalidUpdate() throws Exception {
        for (String json : List.of(
                body("neon", "", ""),
                body("cream", "unknown-track", ""),
                body("cream", "", "not a domain"),
                "{\"headline\":\"\",\"showcaseDemoIds\":[]}")) {
            mockMvc.perform(put("/mini-homes/me").with(authentication(auth(owner)))
                            .contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("일촌평은 맞팔만 쓸 수 있다")
    void onlyMutualFollowCanWriteComment() throws Exception {
        mockMvc.perform(post("/mini-homes/{id}/comments", owner.getId()).with(authentication(auth(stranger)))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"안녕\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/mini-homes/{id}/comments", owner.getId()).with(authentication(auth(friend)))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"일촌 왔어요\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/mini-homes/{id}/comments", owner.getId()).with(authentication(auth(stranger))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].content").value("일촌 왔어요"));
    }

    @Test
    @DisplayName("비밀 방명록은 작성자와 주인만 내용을 본다")
    void secretGuestbookIsHiddenFromOthers() throws Exception {
        mockMvc.perform(post("/mini-homes/{id}/guestbook", owner.getId()).with(authentication(auth(friend)))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"비밀 이야기\",\"secret\":true}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/mini-homes/{id}/guestbook", owner.getId()).with(authentication(auth(stranger))))
                .andExpect(jsonPath("$.data.content[0].secret").value(true))
                .andExpect(jsonPath("$.data.content[0].content").doesNotExist());
        mockMvc.perform(get("/mini-homes/{id}/guestbook", owner.getId()).with(authentication(auth(owner))))
                .andExpect(jsonPath("$.data.content[0].content").value("비밀 이야기"));
        mockMvc.perform(get("/mini-homes/{id}/guestbook", owner.getId()).with(authentication(auth(friend))))
                .andExpect(jsonPath("$.data.content[0].content").value("비밀 이야기"));
    }

    @Test
    @DisplayName("남의 방문은 세고, 내 방문은 세지 않는다")
    void countsOnlyOtherVisits() throws Exception {
        mockMvc.perform(post("/mini-homes/{id}/visits", owner.getId()).with(authentication(auth(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        mockMvc.perform(post("/mini-homes/{id}/visits", owner.getId()).with(authentication(auth(stranger))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.today").value(1))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    private static String body(String skin, String bgm, String domain) {
        return """
                {"headline":"새 소개","quote":"","skin":"%s","domain":"%s","bgm":"%s",
                 "stackTags":"","showcaseDemoIds":[],"featuredDemoId":""}
                """.formatted(skin, domain, bgm);
    }

    private long homeCount() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM mini_homes", Long.class);
        return count == null ? 0 : count;
    }

    private void follow(User follower, User following) {
        jdbc.update("INSERT INTO follows (follower_id, following_id, created_at) VALUES (?, ?, NOW())",
                follower.getId(), following.getId());
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
