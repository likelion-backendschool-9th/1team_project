package com.dipsource.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.entity.user.User;
import com.dipsource.global.auth.AuthCookieNames;
import com.dipsource.global.auth.RefreshTokenHasher;
import com.dipsource.repository.user.RefreshTokenRepository;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.service.user.AuthService;
import com.dipsource.service.user.AuthService.IssuedTokens;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
class AuthControllerTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;
    @Autowired AuthService authService;
    @Autowired RefreshTokenRepository refreshTokenRepository;

    User me;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE TABLE refresh_tokens RESTART IDENTITY CASCADE");
        jdbc.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
        me = persist(User.builder()
                .email("me@test.local")
                .nickname("토큰유저")
                .techStacks(List.of("java"))
                .build());
    }

    @Test
    @DisplayName("정상 갱신하면 새 쿠키를 주고 옛 토큰은 교체 표시된다")
    void refreshRotatesToken() throws Exception {
        IssuedTokens issued = authService.issueTokens(me);

        MvcResult result = mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(AuthCookieNames.REFRESH, issued.refreshToken())))
                .andExpect(status().isNoContent())
                .andReturn();

        String next = result.getResponse().getCookie(AuthCookieNames.REFRESH).getValue();
        assertThat(next).isNotEqualTo(issued.refreshToken());

        em.clear();
        String oldHash = RefreshTokenHasher.sha256(issued.refreshToken());
        LocalDateTime rotatedAt = jdbc.queryForObject(
                "SELECT rotated_at FROM refresh_tokens WHERE token_hash = ?",
                LocalDateTime.class,
                oldHash);
        assertThat(rotatedAt).isNotNull();
        Integer nextRows = jdbc.queryForObject(
                "SELECT COUNT(*) FROM refresh_tokens WHERE token_hash = ?",
                Integer.class,
                RefreshTokenHasher.sha256(next));
        assertThat(nextRows).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 Refresh를 바로 다시 보내면 같은 다음 토큰을 돌려준다")
    void refreshWithinGraceReturnsSameSuccessor() throws Exception {
        IssuedTokens issued = authService.issueTokens(me);
        String original = issued.refreshToken();

        String first = refreshCookie(original);
        String second = refreshCookie(original);

        assertThat(second).isEqualTo(first);
        assertThat(first).isNotEqualTo(original);
        assertThat(refreshTokenRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("A→B→C 교체 후 A로 요청하면 쿠키에 B가 아니라 C를 준다")
    void graceReturnsLatestInChain() throws Exception {
        IssuedTokens issued = authService.issueTokens(me);
        String tokenA = issued.refreshToken();
        String tokenB = refreshCookie(tokenA);
        String tokenC = refreshCookie(tokenB);

        String againFromA = refreshCookie(tokenA);

        assertThat(againFromA).isEqualTo(tokenC);
        assertThat(againFromA).isNotEqualTo(tokenB);
    }

    @Test
    @DisplayName("유예 초과 폐기와 현역 갱신이 동시에 끝나도 현역 토큰이 남지 않는다")
    void concurrentRevokeAndRotateLeavesNoLiveToken() throws Exception {
        IssuedTokens issued = authService.issueTokens(me);
        String tokenA = issued.refreshToken();
        String tokenB = authService.rotate(tokenA).refreshToken();
        jdbc.update(
                "UPDATE refresh_tokens SET rotated_at = ? WHERE token_hash = ?",
                LocalDateTime.now(SEOUL).minusSeconds(31),
                RefreshTokenHasher.sha256(tokenA));

        String familyId = jdbc.queryForObject(
                "SELECT family_id FROM refresh_tokens WHERE token_hash = ?",
                String.class,
                RefreshTokenHasher.sha256(tokenA));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        AtomicInteger unauthorized = new AtomicInteger();
        try {
            pool.submit(() -> {
                await(start);
                try {
                    authService.rotate(tokenA);
                } catch (BusinessException ignored) {
                    unauthorized.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
            pool.submit(() -> {
                await(start);
                try {
                    authService.rotate(tokenB);
                } catch (BusinessException ignored) {
                    unauthorized.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
            start.countDown();
            assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            pool.shutdownNow();
        }

        assertThat(unauthorized.get()).isGreaterThanOrEqualTo(1);
        Integer live = jdbc.queryForObject(
                """
                SELECT COUNT(*) FROM refresh_tokens
                WHERE family_id = ? AND revoked_at IS NULL AND rotated_at IS NULL
                """,
                Integer.class,
                familyId);
        assertThat(live).isZero();
    }

    @Test
    @DisplayName("유예가 지난 옛 토큰 재사용은 401이고 그 로그인 줄을 폐기한다")
    void reuseAfterGraceRevokesFamily() throws Exception {
        IssuedTokens issued = authService.issueTokens(me);
        String original = issued.refreshToken();
        refreshCookie(original);

        jdbc.update(
                "UPDATE refresh_tokens SET rotated_at = ? WHERE token_hash = ?",
                LocalDateTime.now(SEOUL).minusSeconds(31),
                RefreshTokenHasher.sha256(original));
        em.clear();

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(AuthCookieNames.REFRESH, original)))
                .andExpect(status().isUnauthorized());

        Integer stillActive = jdbc.queryForObject(
                "SELECT COUNT(*) FROM refresh_tokens WHERE family_id = (SELECT family_id FROM refresh_tokens WHERE token_hash = ?) AND revoked_at IS NULL",
                Integer.class,
                RefreshTokenHasher.sha256(original));
        assertThat(stillActive).isZero();
    }

    @Test
    @DisplayName("로그아웃 후 그 Refresh로는 갱신할 수 없다")
    void logoutRevokesRefresh() throws Exception {
        IssuedTokens issued = authService.issueTokens(me);

        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie(AuthCookieNames.REFRESH, issued.refreshToken())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(AuthCookieNames.REFRESH, issued.refreshToken())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DB에 없는 Refresh는 401이다")
    void unknownRefreshIsUnauthorized() throws Exception {
        IssuedTokens issued = authService.issueTokens(me);
        jdbc.execute("TRUNCATE TABLE refresh_tokens RESTART IDENTITY CASCADE");
        em.clear();

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(AuthCookieNames.REFRESH, issued.refreshToken())))
                .andExpect(status().isUnauthorized());
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private String refreshCookie(String refreshToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(AuthCookieNames.REFRESH, refreshToken)))
                .andExpect(status().isNoContent())
                .andReturn();
        return result.getResponse().getCookie(AuthCookieNames.REFRESH).getValue();
    }

    private <T> T persist(T entity) {
        return tx.execute(status -> {
            em.persist(entity);
            em.flush();
            return entity;
        });
    }
}
