package com.dipsource.controller.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedUserId;
import com.dipsource.entity.user.User;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.repository.feed.FeedLikeRepository;
import com.dipsource.repository.feed.FeedRepository;
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

/**
 * 좋아요 API 통합 테스트.
 * 인증·멱등·like_count 비정규화까지 실제 PostgreSQL에서 검증한다.
 */
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test"
})
class FeedLikeApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;
    @Autowired FeedRepository feedRepository;
    @Autowired FeedLikeRepository feedLikeRepository;

    User author;
    User liker;
    Feed feed;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE feed_likes, feeds, users, user_tag_profile, feed_tags RESTART IDENTITY CASCADE");
        author = persist(User.builder().email("author@test.local").nickname("author").techStacks(List.of()).build());
        liker = persist(User.builder().email("liker@test.local").nickname("liker").techStacks(List.of()).build());
        feed = persist(Feed.builder().userId(author.getId()).content("좋아요 테스트 피드").build());
    }

    @Test
    @DisplayName("비로그인은 401")
    void unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/feeds/{id}/like", feed.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/feeds/{id}/like", feed.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("좋아요 → 카운트 +1, 재요청해도 +1만 유지 (멱등)")
    void likeIdempotent() throws Exception {
        mockMvc.perform(post("/api/v1/feeds/{id}/like", feed.getId()).with(authentication(auth(liker))))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/feeds/{id}/like", feed.getId()).with(authentication(auth(liker))))
                .andExpect(status().isNoContent());

        assertThat(feedLikeRepository.existsById(new FeedUserId(liker.getId(), feed.getId()))).isTrue();
        assertThat(feedRepository.findById(feed.getId()).orElseThrow().getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 취소 → 카운트 0, 재취소해도 0 유지")
    void unlikeIdempotent() throws Exception {
        mockMvc.perform(post("/api/v1/feeds/{id}/like", feed.getId()).with(authentication(auth(liker))))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/feeds/{id}/like", feed.getId()).with(authentication(auth(liker))))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/feeds/{id}/like", feed.getId()).with(authentication(auth(liker))))
                .andExpect(status().isNoContent());

        assertThat(feedLikeRepository.existsById(new FeedUserId(liker.getId(), feed.getId()))).isFalse();
        assertThat(feedRepository.findById(feed.getId()).orElseThrow().getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("없는 피드는 404")
    void missingFeed() throws Exception {
        mockMvc.perform(post("/api/v1/feeds/{id}/like", 999_999L).with(authentication(auth(liker))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("쿼리 userId로는 남을 대신 좋아요할 수 없다 — JWT 사용자만 적용")
    void ignoresQueryUserId() throws Exception {
        mockMvc.perform(post("/api/v1/feeds/{id}/like", feed.getId())
                        .param("userId", String.valueOf(author.getId()))
                        .with(authentication(auth(liker))))
                .andExpect(status().isNoContent());

        assertThat(feedLikeRepository.existsById(new FeedUserId(liker.getId(), feed.getId()))).isTrue();
        assertThat(feedLikeRepository.existsById(new FeedUserId(author.getId(), feed.getId()))).isFalse();
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
