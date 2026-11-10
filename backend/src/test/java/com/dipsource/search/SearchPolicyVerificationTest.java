package com.dipsource.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.dipsource.entity.post.Post;
import com.dipsource.entity.user.User;
import com.dipsource.service.search.sync.SearchOutboxWorker;
import com.dipsource.service.search.sync.SearchReindexService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 변경 직후·캐시 재사용 중에 권한과 노출 정책이 유지되는지 본다.
 * 블로그 CRUD API가 아직 없어서 글 변경은 엔티티를 직접 바꿔 시험한다. 실제 API 경로는 검증하지 못한다.
 */
@AutoConfigureMockMvc
@SpringBootTest(properties = {AbstractSearchVerification.JWT, AbstractSearchVerification.GITHUB_ID, AbstractSearchVerification.GITHUB_SECRET,
        "search.elasticsearch.enabled=true", "search.elasticsearch.index-prefix=policytest",
        "search.elasticsearch.bootstrap-on-empty=false", "search.engine=elasticsearch",
        "search.fallback-to-postgres=false", "search.sync.poll-interval=PT1H"})
class SearchPolicyVerificationTest extends AbstractSearchVerification {

    @DynamicPropertySource
    static void elasticsearch(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", SearchContainers::elasticsearchUri);
    }

    @Autowired
    SearchReindexService reindexService;
    @Autowired
    SearchOutboxWorker worker;
    @Autowired
    EntityManager em;
    @Autowired
    TransactionTemplate tx;

    long author;
    long viewer;

    @BeforeEach
    void setUp() {
        reset();
        author = user("작성자", null);
        viewer = user("검색자", null);
    }

    private void reindex() throws Exception {
        reindexService.rebuildAll();
        jdbc.execute("TRUNCATE search_outbox RESTART IDENTITY");
    }

    private void drain() {
        while (worker.processBatch() > 0) {
            // 대기열이 빌 때까지 처리한다
        }
    }

    private void changePost(long id, java.util.function.Consumer<Post> change) {
        tx.executeWithoutResult(status -> change.accept(em.find(Post.class, id)));
    }

    private long many(long owner, String word, int count) {
        long first = 0;
        for (int i = 0; i < count; i++) {
            long id = post(owner, word + " 글 " + i, "본문");
            first = first == 0 ? id : first;
        }
        return first;
    }

    @Test
    @DisplayName("글 수정: 동기화 전에도 응답은 최신 내용이고, 동기화 후 옛 검색어로는 찾히지 않는다")
    void postUpdate() throws Exception {
        long id = post(author, "Kafka 옛제목", "본문");
        reindex();

        changePost(id, p -> p.update("Redis 새제목", p.getCategory(), "본문", null, null, false, 0));
        assertThat(search("kafka", "post").ids()).as("동기화 전에는 옛 색인으로 찾히지만 최신 글을 응답").contains((int) id);
        assertThat(search("redis", "post").total()).as("새 검색어는 아직 없음").isZero();

        drain();
        assertThat(search("kafka", "post").total()).isZero();
        assertThat(search("redis", "post").ids()).containsExactly((int) id);
    }

    @Test
    @DisplayName("글 삭제: 동기화 전에도 결과와 총개수에서 빠진다")
    void postDelete() throws Exception {
        long id = post(author, "Kafka 글", "본문");
        post(author, "Kafka 다른 글", "본문");
        reindex();

        changePost(id, Post::delete);
        Page before = search("kafka", "post");
        assertThat(before.ids()).doesNotContain((int) id);
        assertThat(before.total()).isEqualTo(1);

        drain();
        assertThat(search("kafka", "post").total()).isEqualTo(1);
    }

    @Test
    @DisplayName("운영자 숨김: 동기화 전에도 빠진다")
    void postHide() throws Exception {
        long id = post(author, "Kafka 글", "본문");
        reindex();

        changePost(id, Post::hide);
        assertThat(search("kafka", "post").ids()).isEmpty();
    }

    @Test
    @DisplayName("무료 글을 유료로 바꾸면 동기화 전에도 본문 단어로 찾히지 않는다")
    void paidConversionBeforeSync() throws Exception {
        long id = post(author, "Kafka 글", "비밀단어 본문");
        reindex();

        changePost(id, p -> p.update("Kafka 글", p.getCategory(), "비밀단어 본문", null, null, true, 100));
        assertThat(search("비밀단어", "post").ids()).doesNotContain((int) id);
        drain();
        assertThat(search("비밀단어", "post").ids()).doesNotContain((int) id);
    }

    @Test
    @DisplayName("회원 탈퇴: 동기화 전에도 사용자 검색에서 빠지고, 동기화 후 숨김·방문 기록이 지워진다")
    void withdrawal() throws Exception {
        long id = user("springdev", "소개");
        jdbc.update("INSERT INTO search_hidden_results(user_id, target_type, target_id, created_at) VALUES (?, 'POST', 1, now())", id);
        jdbc.update("INSERT INTO search_visits(user_id, target_type, target_id, query_norm, visit_count, last_visited_at) VALUES (?, 'POST', 1, 'q', 1, now())", id);
        reindex();

        tx.executeWithoutResult(status -> em.find(User.class, id).withdraw());
        assertThat(search("springdev", "user").ids()).isEmpty();

        drain();
        assertThat(search("springdev", "user").total()).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM search_hidden_results WHERE user_id = ?", Long.class, id)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM search_visits WHERE user_id = ?", Long.class, id)).isZero();
    }

    @Test
    @DisplayName("정지 회원: 사용자 검색에서는 빠지고 글은 기존 정책대로 남는다")
    void suspension() throws Exception {
        long id = user("springdev", "소개");
        long written = post(id, "Kafka 정지회원 글", "본문");
        reindex();

        tx.executeWithoutResult(status -> em.find(User.class, id).suspend());
        drain();
        assertThat(search("springdev", "user").total()).isZero();
        assertThat(search("kafka", "post").ids()).containsExactly((int) written);
    }

    @Test
    @DisplayName("차단: 새로 검색하면 바로 반영된다")
    void blockOnFirstPage() throws Exception {
        long other = user("작성자2", null);
        post(other, "Kafka 글", "본문");
        reindex();
        assertThat(search(viewer, "kafka", "post", 0, 20).total()).isEqualTo(1);

        block(viewer, other);
        assertThat(search(viewer, "kafka", "post", 0, 20).total()).isZero();
    }

    @Test
    @DisplayName("차단 직후 이미 만들어진 재정렬 캐시로 다음 페이지를 봐도 차단한 작성자가 보이지 않는다")
    void blockOnCachedPage() throws Exception {
        long other = user("작성자2", null);
        for (int i = 0; i < 30; i++) {
            post(i % 2 == 0 ? other : author, "Kafka 글 " + i, "본문");
        }
        reindex();
        search(viewer, "kafka", "post", 0, 10);

        block(viewer, other);
        var second = search(viewer, "kafka", "post", 1, 10).ids();
        var authors = jdbc.queryForList("SELECT user_id FROM posts WHERE id = ANY(?)", Long.class,
                (Object) second.stream().map(Integer::longValue).toArray(Long[]::new));
        assertThat(authors).doesNotContain(other);
    }

    @Test
    @DisplayName("숨김: 캐시가 있어도 다음 페이지에서 바로 빠진다")
    void hideOnCachedPage() throws Exception {
        long first = many(author, "Kafka", 30);
        reindex();
        search(viewer, "kafka", "post", 0, 10);

        // 실제 숨김 API를 거쳐야 캐시가 비워진다
        var principal = new com.dipsource.global.auth.UserPrincipal(viewer, "USER");
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/search/hidden")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"targetType\":\"post\",\"targetId\":" + first + "}")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()));

        for (int page = 1; page < 3; page++) {
            assertThat(search(viewer, "kafka", "post", page, 10).ids()).doesNotContain((int) first);
        }
    }

    @Test
    @DisplayName("캐시는 사용자별로 분리된다: 차단 결과가 다른 사용자·비로그인에게 섞이지 않는다")
    void cacheIsPerViewer() throws Exception {
        long other = user("작성자2", null);
        post(other, "Kafka 글", "본문");
        long stranger = user("다른사람", null);
        block(viewer, other);
        reindex();

        assertThat(search(viewer, "kafka", "post", 0, 20).total()).isZero();
        assertThat(search(stranger, "kafka", "post", 0, 20).total()).isEqualTo(1);
        assertThat(search(null, "kafka", "post", 0, 20).total()).isEqualTo(1);
    }

    @Test
    @DisplayName("캐시된 페이지에서 삭제된 글은 노출되지 않는다")
    void deletedOnCachedPage() throws Exception {
        many(author, "Kafka", 30);
        reindex();
        var first = search(null, "kafka", "post", 0, 10);
        var second = search(null, "kafka", "post", 1, 10);
        long target = second.ids().get(0);

        changePost(target, Post::delete);
        assertThat(search(null, "kafka", "post", 1, 10).ids()).doesNotContain((int) target);
        assertThat(first.total()).isEqualTo(30);
    }

    @Test
    @DisplayName("삭제된 글의 제목 단어는 동기화되면 자동완성에 나오지 않는다")
    void autocompleteAfterDelete() throws Exception {
        long id = post(author, "비공개예정단어zxq 이야기", "본문");
        reindex();
        assertThat(autocomplete("비공개예정")).isNotEmpty();

        changePost(id, Post::delete);
        drain();
        assertThat(autocomplete("비공개예정")).isEmpty();
    }

    @Test
    @DisplayName("글 수정 API로 태그를 전부 지우면 그 태그로 찾히지 않는다")
    void tagRemovalThroughApi() throws Exception {
        long id = post(author, "Kafka 운영 기록", "본문 내용을 충분히 길게 작성한 글입니다");
        long tagId = jdbc.queryForObject("INSERT INTO tags(name) VALUES ('kafka') RETURNING id", Long.class);
        jdbc.update("INSERT INTO post_tags(post_id, tag_id) VALUES (?, ?)", id, tagId);
        reindex();
        assertThat(search("#kafka", "post").ids()).containsExactly((int) id);

        var principal = new com.dipsource.global.auth.UserPrincipal(author, "USER");
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/posts/" + id)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Kafka 운영 기록\",\"category\":\"backend\",\"tags\":[],\"content\":\"본문 내용을 충분히 길게 작성한 글입니다\",\"price\":0}")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is2xxSuccessful());
        drain();

        assertThat(search("#kafka", "post").total()).isZero();
    }
}
