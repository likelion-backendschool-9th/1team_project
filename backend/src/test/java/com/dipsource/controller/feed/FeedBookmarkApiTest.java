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
import com.dipsource.repository.feed.FeedBookmarkRepository;
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
 * 북마크 API 통합 테스트.
 * 인증·멱등·쿼리 userId 무시까지 실제 PostgreSQL에서 검증한다.
 */
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test"
})
class FeedBookmarkApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;
    @Autowired FeedRepository feedRepository;
    @Autowired FeedBookmarkRepository feedBookmarkRepository;

    User author;
    User reader;
    Feed feed;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE feed_bookmarks, feed_likes, feeds, users, user_tag_profile, feed_tags RESTART IDENTITY CASCADE");
        author = persist(User.builder().email("author@test.local").nickname("author").techStacks(List.of()).build());
        reader = persist(User.builder().email("reader@test.local").nickname("reader").techStacks(List.of()).build());
        feed = persist(Feed.builder().userId(author.getId()).content("북마크 테스트 피드").build());
    }

    @Test
    @DisplayName("비로그인은 401")
    void unauthenticated() throws Exception {
        mockMvc.perform(post("/feeds/{id}/bookmark", feed.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/feeds/{id}/bookmark", feed.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("북마크 → 재요청해도 한 줄만 유지 (멱등)")
    void bookmarkIdempotent() throws Exception {
        mockMvc.perform(post("/feeds/{id}/bookmark", feed.getId()).with(authentication(auth(reader))))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/feeds/{id}/bookmark", feed.getId()).with(authentication(auth(reader))))
                .andExpect(status().isNoContent());

        assertThat(feedBookmarkRepository.existsById(new FeedUserId(reader.getId(), feed.getId()))).isTrue();
        assertThat(feedRepository.findById(feed.getId()).orElseThrow().getLikeCount()).isZero();
    }

    @Test
    @DisplayName("북마크 취소 → 재취소해도 없음 유지")
    void unbookmarkIdempotent() throws Exception {
        mockMvc.perform(post("/feeds/{id}/bookmark", feed.getId()).with(authentication(auth(reader))))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/feeds/{id}/bookmark", feed.getId()).with(authentication(auth(reader))))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/feeds/{id}/bookmark", feed.getId()).with(authentication(auth(reader))))
                .andExpect(status().isNoContent());

        assertThat(feedBookmarkRepository.existsById(new FeedUserId(reader.getId(), feed.getId()))).isFalse();
    }

    @Test
    @DisplayName("없는 피드는 404")
    void missingFeed() throws Exception {
        mockMvc.perform(post("/feeds/{id}/bookmark", 999_999L).with(authentication(auth(reader))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("쿼리 userId로는 남을 대신 북마크할 수 없다")
    void ignoresQueryUserId() throws Exception {
        mockMvc.perform(post("/feeds/{id}/bookmark", feed.getId())
                        .param("userId", String.valueOf(author.getId()))
                        .with(authentication(auth(reader))))
                .andExpect(status().isNoContent());

        assertThat(feedBookmarkRepository.existsById(new FeedUserId(reader.getId(), feed.getId()))).isTrue();
        assertThat(feedBookmarkRepository.existsById(new FeedUserId(author.getId(), feed.getId()))).isFalse();
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
