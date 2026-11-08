package com.dipsource.search;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.dipsource.global.auth.UserPrincipal;
import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** 추가 검증 테스트 공통. 데이터는 PostgreSQL에 직접 넣고 응답은 실제 API로 받는다. */
abstract class AbstractSearchVerification {

    static final String JWT = "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000";
    static final String GITHUB_ID = "spring.security.oauth2.client.registration.github.client-id=test";
    static final String GITHUB_SECRET = "spring.security.oauth2.client.registration.github.client-secret=test";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JdbcTemplate jdbc;

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SearchContainers.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", SearchContainers.POSTGRES::getUsername);
        registry.add("spring.datasource.password", SearchContainers.POSTGRES::getPassword);
    }

    record Page(int status, List<Integer> ids, long total, int totalPages) {
    }

    void reset() {
        jdbc.execute("TRUNCATE posts, feeds, demos, users, tags, follows, blocks, post_tags, feed_tags, demo_tags, "
                + "search_outbox, search_hidden_results, search_visits RESTART IDENTITY CASCADE");
    }

    long user(String nickname, String bio) {
        return jdbc.queryForObject("""
                INSERT INTO users(email, nickname, bio, tech_stacks, role, status, chip_balance,
                                  blog_subscription_price, created_at, updated_at)
                VALUES (?, ?, ?, '[]', 'USER', 'ACTIVE', 0, 0, now(), now()) RETURNING id
                """, Long.class, nickname + "@test.local", nickname, bio);
    }

    long post(long author, String title, String content) {
        return jdbc.queryForObject("""
                INSERT INTO posts(user_id, title, content, is_paid, price_chips, status, view_count, like_count,
                                  comment_count, created_at, updated_at)
                VALUES (?, ?, ?, false, 0, 'ACTIVE', 0, 0, 0, now(), now()) RETURNING id
                """, Long.class, author, title, content);
    }

    void block(long blocker, long blocked) {
        jdbc.update("INSERT INTO blocks(blocker_id, blocked_id, created_at) VALUES (?, ?, now())", blocker, blocked);
    }

    Page search(Long viewer, String q, String type, int page, int size, String... extra) throws Exception {
        MockHttpServletRequestBuilder request = get("/api/v1/search").param("q", q).param("type", type)
                .param("page", String.valueOf(page)).param("size", String.valueOf(size));
        for (int i = 0; i + 1 < extra.length; i += 2) {
            request.param(extra[i], extra[i + 1]);
        }
        if (viewer != null) {
            UserPrincipal principal = new UserPrincipal(viewer, "USER");
            request.with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())));
        }
        var response = mockMvc.perform(request).andReturn().getResponse();
        if (response.getStatus() != 200) {
            return new Page(response.getStatus(), List.of(), 0, 0);
        }
        String body = response.getContentAsString();
        List<Integer> ids = JsonPath.read(body, "$.data.content[*].id");
        return new Page(200, new ArrayList<>(ids), ((Number) JsonPath.read(body, "$.data.totalElements")).longValue(),
                JsonPath.read(body, "$.data.totalPages"));
    }

    Page search(String q, String type) throws Exception {
        return search(null, q, type, 0, 20);
    }

    List<String> autocomplete(String q) throws Exception {
        String body = mockMvc.perform(get("/api/v1/search/autocomplete").param("q", q)).andReturn().getResponse()
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return JsonPath.read(body, "$.data[*].text");
    }
}
