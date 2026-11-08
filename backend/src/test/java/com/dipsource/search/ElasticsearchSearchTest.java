package com.dipsource.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.dto.search.SearchType;
import com.dipsource.service.search.index.SearchIndexManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Elasticsearch 검색 경로의 동작 테스트. 실제 PostgreSQL·Elasticsearch 컨테이너에서 돈다. */
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test",
        "search.elasticsearch.enabled=true",
        "search.engine=elasticsearch",
        "search.fallback-to-postgres=false"
})
class ElasticsearchSearchTest {

    @DynamicPropertySource
    static void containers(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SearchContainers.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", SearchContainers.POSTGRES::getUsername);
        registry.add("spring.datasource.password", SearchContainers.POSTGRES::getPassword);
        registry.add("spring.elasticsearch.uris", SearchContainers::elasticsearchUri);
    }

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    SearchIndexManager indexManager;

    long author;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE posts, feeds, demos, users, tags, follows, blocks RESTART IDENTITY CASCADE");
        author = user("작성자", "글 쓰는 사람");
    }

    @Test
    @DisplayName("제목과 본문에 나뉜 단어도 and로 찾고, 한쪽만 있는 글은 뺀다")
    void andAcrossFields() throws Exception {
        post("Spring 트랜잭션", "JPA 영속성 컨텍스트", "ACTIVE");
        post("Spring 시작하기", "프레임워크 소개", "ACTIVE");
        reindex();

        search("spring jpa", "op", "and").andExpect(jsonPath("$.data.totalElements").value(1));
        search("spring jpa", "op", "or").andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @DisplayName("숨김·삭제 글은 색인되지 않는다")
    void hiddenAndDeletedNotIndexed() throws Exception {
        post("Spring 공개", "본문", "ACTIVE");
        post("Spring 숨김", "본문", "HIDDEN");
        post("Spring 삭제", "본문", "DELETED");
        reindex();

        search("spring").andExpect(jsonPath("$.data.content[*].title", contains("Spring 공개")));
    }

    @Test
    @DisplayName("색인 뒤에 삭제된 글은 인덱스에 남아 있어도 노출되지 않는다")
    void staleIndexDoesNotLeak() throws Exception {
        long id = post("Spring 곧 삭제", "본문", "ACTIVE");
        reindex();
        jdbc.update("UPDATE posts SET status = 'DELETED', deleted_at = now() WHERE id = ?", id);

        search("spring").andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("유료 글 본문은 검색되지 않고 제목은 검색된다")
    void paidBodyIsNotSearchable() throws Exception {
        jdbc.update("""
                INSERT INTO posts(user_id, title, content, is_paid, price_chips, status, view_count, like_count,
                                  comment_count, created_at, updated_at)
                VALUES (?, 'Kafka 운영', '비밀단어 본문', true, 100, 'ACTIVE', 0, 0, 0, now(), now())
                """, author);
        reindex();

        search("비밀단어").andExpect(jsonPath("$.data.totalElements").value(0));
        search("kafka").andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @ParameterizedTest(name = "q=[{0}]")
    @ValueSource(strings = {"a", "a ", "스", " "})
    @DisplayName("한 글자 일반 검색어는 거절한다")
    void tooShort(String q) throws Exception {
        search(q).andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "q=[{0}]")
    @ValueSource(strings = {"AI", "DB", "스프", "C", "R", "#c"})
    @DisplayName("두 글자 이상과 사전의 한 글자 용어는 허용한다")
    void allowed(String q) throws Exception {
        reindex();
        search(q).andExpect(status().isOk());
    }

    @Test
    @DisplayName("이모지는 코드 포인트로 세서 한 글자로 본다")
    void emojiIsOneCharacter() throws Exception {
        search("😀").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("코드 포인트 기준 50자까지 허용한다. UTF-16으로 세면 넘는 이모지 50자도 통과한다")
    void maxLengthInCodePoints() throws Exception {
        reindex();
        search("😀".repeat(50)).andExpect(status().isOk());
        search("😀".repeat(51)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("같은 점수는 최신순, 그다음 ID로 순서가 고정되어 페이지 간 중복·누락이 없다")
    void stablePagination() throws Exception {
        for (int i = 0; i < 25; i++) {
            post("Spring 동일 제목", "같은 본문", "ACTIVE");
        }
        reindex();

        List<Integer> seen = new ArrayList<>();
        for (int page = 0; page < 3; page++) {
            String body = mockMvc.perform(get("/api/v1/search").param("q", "spring").param("type", "post")
                            .param("page", String.valueOf(page)).param("size", "10"))
                    .andReturn().getResponse().getContentAsString();
            seen.addAll(com.jayway.jsonpath.JsonPath.read(body, "$.data.content[*].id"));
        }
        assertThat(seen).hasSize(25).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("전체 검색은 대상별 묶음으로 응답한다")
    void searchAll() throws Exception {
        post("Spring 글", "본문", "ACTIVE");
        user("springdev", "소개");
        reindex();

        mockMvc.perform(get("/api/v1/search").param("q", "spring"))
                .andExpect(jsonPath("$.data.posts.totalElements").value(1))
                .andExpect(jsonPath("$.data.users.totalElements").value(1))
                .andExpect(jsonPath("$.data.demos.totalElements").value(0))
                .andExpect(jsonPath("$.data.feeds.totalElements").value(0));
    }

    private ResultActions search(String q, String... extra) throws Exception {
        var request = get("/api/v1/search").param("q", q).param("type", "post");
        for (int i = 0; i + 1 < extra.length; i += 2) {
            request.param(extra[i], extra[i + 1]);
        }
        return mockMvc.perform(request);
    }

    private long user(String nickname, String bio) {
        return jdbc.queryForObject("""
                INSERT INTO users(email, nickname, bio, tech_stacks, role, status, chip_balance,
                                  blog_subscription_price, created_at, updated_at)
                VALUES (?, ?, ?, '[]', 'USER', 'ACTIVE', 0, 0, now(), now()) RETURNING id
                """, Long.class, nickname + "@test.local", nickname, bio);
    }

    private long post(String title, String content, String status) {
        return jdbc.queryForObject("""
                INSERT INTO posts(user_id, title, content, is_paid, price_chips, status, view_count, like_count,
                                  comment_count, created_at, updated_at, deleted_at)
                VALUES (?, ?, ?, false, 0, ?, 0, 0, 0, now(), now(),
                        CASE WHEN ? = 'DELETED' THEN now() END) RETURNING id
                """, Long.class, author, title, content, status, status);
    }

    private void reindex() throws Exception {
        for (SearchType type : SearchIndexManager.TYPES) {
            indexManager.rebuild(type);
        }
    }
}
