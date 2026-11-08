package com.dipsource.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.dipsource.dto.search.SearchType;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.user.User;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.repository.feed.FeedRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.search.index.SearchIndexManager;
import com.dipsource.service.search.sync.SearchOutboxWorker;
import com.dipsource.service.search.sync.SearchReindexService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

/** PostgreSQL → Elasticsearch 동기화와 재색인. 실제 JPA 변경이 대기열을 거쳐 색인되는지 본다. */
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test",
        "search.elasticsearch.enabled=true",
        "search.elasticsearch.index-prefix=synctest",
        "search.elasticsearch.bootstrap-on-empty=false",
        "search.engine=elasticsearch",
        "search.fallback-to-postgres=false",
        "search.sync.poll-interval=PT1H"
})
class SearchSyncTest {

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
    @Autowired
    SearchOutboxWorker worker;
    @Autowired
    SearchReindexService reindexService;
    @Autowired
    FeedRepository feedRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TransactionTemplate tx;
    @Autowired
    ElasticsearchClient client;

    User author;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.execute("TRUNCATE posts, feeds, demos, users, tags, follows, blocks, search_outbox RESTART IDENTITY CASCADE");
        author = tx.execute(status -> userRepository.save(User.builder()
                .email("author@test.local").nickname("작성자").techStacks(List.of()).build()));
        jdbc.execute("TRUNCATE search_outbox RESTART IDENTITY");
        reindexService.rebuildAll();
    }

    private Feed saveFeed(String content) {
        return tx.execute(status -> feedRepository.save(Feed.builder().userId(author.getId()).content(content).build()));
    }

    private int drain() {
        int total = 0;
        int processed;
        while ((processed = worker.processBatch()) > 0) {
            total += processed;
        }
        return total;
    }

    private long searchTotal(String q) throws Exception {
        String body = mockMvc.perform(get("/api/v1/search").param("q", q).param("type", "feed"))
                .andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(body, "$.data.totalElements")).longValue();
    }

    @Test
    @DisplayName("생성·수정·삭제가 대기열을 거쳐 검색에 반영된다")
    void createUpdateDelete() throws Exception {
        Feed feed = saveFeed("spring 이야기");
        assertThat(drain()).isGreaterThan(0);
        assertThat(searchTotal("spring")).isEqualTo(1);

        tx.executeWithoutResult(status -> feedRepository.findById(feed.getId()).orElseThrow().update("kafka 이야기"));
        drain();
        assertThat(searchTotal("spring")).isZero();
        assertThat(searchTotal("kafka")).isEqualTo(1);

        tx.executeWithoutResult(status -> feedRepository.findById(feed.getId()).orElseThrow().delete());
        drain();
        assertThat(searchTotal("kafka")).isZero();
        assertThat(indexManager.count(SearchType.FEED)).isZero();
    }

    @Test
    @DisplayName("트랜잭션이 롤백되면 대기열에도 남지 않는다")
    void rollbackLeavesNoEvent() {
        try {
            tx.executeWithoutResult(status -> {
                feedRepository.save(Feed.builder().userId(author.getId()).content("롤백").build());
                feedRepository.flush();
                throw new IllegalStateException("롤백");
            });
        } catch (IllegalStateException ignored) {
            // 의도한 예외
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM search_outbox", Long.class)).isZero();
    }

    @Test
    @DisplayName("같은 문서의 중복 이벤트는 한 번만 반영되고 멱등하다")
    void duplicateEvents() throws Exception {
        Feed feed = saveFeed("spring 이야기");
        reindexService.reindexOne(SearchType.FEED, feed.getId());
        reindexService.reindexOne(SearchType.FEED, feed.getId());

        drain();
        assertThat(searchTotal("spring")).isEqualTo(1);
        assertThat(indexManager.count(SearchType.FEED)).isEqualTo(1);
    }

    @Test
    @DisplayName("회원 닉네임 변경이 반영되고, 정지하면 검색에서 빠진다")
    void userChanges() throws Exception {
        tx.executeWithoutResult(status -> userRepository.findById(author.getId()).orElseThrow()
                .updateProfile("springdev", null, "소개", List.of("Java"), null));
        drain();
        String found = mockMvc.perform(get("/api/v1/search").param("q", "springdev").param("type", "user"))
                .andReturn().getResponse().getContentAsString();
        assertThat(found).contains("springdev");

        tx.executeWithoutResult(status -> userRepository.findById(author.getId()).orElseThrow().suspend());
        drain();
        assertThat(indexManager.count(SearchType.USER)).isZero();
    }

    @Test
    @DisplayName("색인과 무관한 필드(마지막 활동 시각)가 바뀌면 대기열에 넣지 않는다")
    void irrelevantFieldIgnored() {
        tx.executeWithoutResult(status -> userRepository.findById(author.getId()).orElseThrow().markActive());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM search_outbox", Long.class)).isZero();
    }

    @Test
    @DisplayName("검색 엔진이 실패하면 대기열에 남아 재시도된다")
    void failureIsRetried() throws Exception {
        Feed feed = saveFeed("spring 이야기");
        // 인덱스를 지워서 색인 실패를 만든다(Alias가 없어지면 자동 생성이 strict 매핑과 달라지므로 Alias를 닫는다)
        String alias = indexManager.alias(SearchType.FEED);
        String index = client.indices().getAlias(a -> a.name(alias)).result().keySet().iterator().next();
        client.indices().close(c -> c.index(index));

        worker.processBatch();
        assertThat(jdbc.queryForMap("SELECT attempts, processed_at FROM search_outbox WHERE target_id = ?", feed.getId()))
                .containsEntry("attempts", 1).containsEntry("processed_at", null);

        client.indices().open(o -> o.index(index));
        jdbc.update("UPDATE search_outbox SET next_attempt_at = now()");
        worker.processBatch();
        assertThat(searchTotal("spring")).isEqualTo(1);
    }

    @Test
    @DisplayName("정합성 점검은 누락 문서와 잔존 문서를 복구한다")
    void reconcile() throws Exception {
        // 대기열 없이 원본만 추가(누락)
        jdbc.update("""
                INSERT INTO feeds(user_id, content, status, like_count, comment_count, repost_count, created_at, updated_at)
                VALUES (?, 'spring 누락', 'ACTIVE', 0, 0, 0, now(), now())
                """, author.getId());
        // 색인된 뒤 대기열 없이 삭제(잔존)
        Feed stale = saveFeed("kafka 잔존");
        drain();
        jdbc.update("UPDATE feeds SET status = 'DELETED', deleted_at = now() WHERE id = ?", stale.getId());

        Map<String, Integer> result = reindexService.reconcile();
        drain();

        assertThat(result.get("FEED")).isEqualTo(2);
        assertThat(searchTotal("spring")).isEqualTo(1);
        assertThat(indexManager.indexedIds(SearchType.FEED)).doesNotContain(stale.getId());
    }

    @Test
    @DisplayName("재색인은 Alias를 옮기고 그동안의 변경도 반영한다")
    void rebuildReplaysChanges() throws Exception {
        saveFeed("spring 재색인");
        drain();
        String alias = indexManager.alias(SearchType.FEED);
        String before = client.indices().getAlias(a -> a.name(alias)).result().keySet().iterator().next();

        reindexService.rebuildAll();
        drain();

        String after = client.indices().getAlias(a -> a.name(alias)).result().keySet().iterator().next();
        assertThat(after).isNotEqualTo(before);
        assertThat(searchTotal("spring")).isEqualTo(1);
    }

    @Test
    @DisplayName("운영 API는 관리자만 쓸 수 있다")
    void adminOnly() throws Exception {
        UserPrincipal user = new UserPrincipal(1L, "USER");
        UserPrincipal admin = new UserPrincipal(2L, "ADMIN");
        mockMvc.perform(post("/api/v1/admin/search/reconcile").with(csrf())
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/search/reconcile").with(csrf())
                        .with(authentication(new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/search/status")
                        .with(authentication(new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()))))
                .andExpect(jsonPath("$.data.outbox.pending").isNumber());
    }
}
