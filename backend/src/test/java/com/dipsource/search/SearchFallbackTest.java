package com.dipsource.search;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** 검색 엔진에 연결할 수 없을 때 PostgreSQL 검색으로 대체되는지 본다. */
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test",
        "search.elasticsearch.enabled=true",
        "search.elasticsearch.bootstrap-on-empty=false",
        "search.engine=elasticsearch",
        "search.sync.poll-interval=PT1H",
        "spring.elasticsearch.uris=http://localhost:1",
        "spring.elasticsearch.connection-timeout=200ms"
})
class SearchFallbackTest {

    @DynamicPropertySource
    static void containers(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SearchContainers.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", SearchContainers.POSTGRES::getUsername);
        registry.add("spring.datasource.password", SearchContainers.POSTGRES::getPassword);
    }

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JdbcTemplate jdbc;

    @Test
    @DisplayName("Elasticsearch가 죽어도 검색과 자동완성은 PostgreSQL로 응답한다")
    void fallsBack() throws Exception {
        jdbc.execute("TRUNCATE posts, users RESTART IDENTITY CASCADE");
        long author = jdbc.queryForObject("""
                INSERT INTO users(email, nickname, tech_stacks, role, status, chip_balance, blog_subscription_price, created_at, updated_at)
                VALUES ('a@test.local', '작성자', '[]', 'USER', 'ACTIVE', 0, 0, now(), now()) RETURNING id
                """, Long.class);
        jdbc.update("""
                INSERT INTO posts(user_id, title, content, is_paid, price_chips, status, view_count, like_count, comment_count, created_at, updated_at)
                VALUES (?, 'Spring 글', '본문', false, 0, 'ACTIVE', 0, 0, 0, now(), now())
                """, author);

        mockMvc.perform(get("/search").param("q", "spring").param("type", "post"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
        mockMvc.perform(get("/search").param("q", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.posts.totalElements").value(1));
        mockMvc.perform(get("/search/autocomplete").param("q", "spr"))
                .andExpect(status().isOk());
    }
}
