package com.dipsource.search;

import static org.assertj.core.api.Assertions.assertThat;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.dipsource.dto.search.SearchType;
import com.dipsource.entity.feed.Feed;
import com.dipsource.repository.feed.FeedRepository;
import com.dipsource.service.search.index.SearchIndexManager;
import com.dipsource.service.search.sync.SearchOutboxRepository;
import com.dipsource.service.search.sync.SearchOutboxWorker;
import com.dipsource.service.search.sync.SearchReindexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

/** 색인 불가 상태에서 복구했을 때 누락이 없는지 본다. 인덱스를 닫아서 쓰기 실패를 만든다. 실제 컨테이너 정지는 outage-recovery.py가 시험한다. */
@AutoConfigureMockMvc
@SpringBootTest(properties = {AbstractSearchVerification.JWT, AbstractSearchVerification.GITHUB_ID, AbstractSearchVerification.GITHUB_SECRET,
        "search.elasticsearch.enabled=true", "search.elasticsearch.index-prefix=recoverytest",
        "search.elasticsearch.bootstrap-on-empty=false", "search.engine=elasticsearch",
        "search.fallback-to-postgres=false", "search.sync.poll-interval=PT1H"})
class SearchRecoveryVerificationTest extends AbstractSearchVerification {

    @DynamicPropertySource
    static void elasticsearch(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", SearchContainers::elasticsearchUri);
    }

    @Autowired
    SearchReindexService reindexService;
    @Autowired
    SearchOutboxWorker worker;
    @Autowired
    SearchOutboxRepository outbox;
    @Autowired
    SearchIndexManager indexManager;
    @Autowired
    FeedRepository feedRepository;
    @Autowired
    TransactionTemplate tx;
    @Autowired
    ElasticsearchClient client;

    long author;

    @BeforeEach
    void setUp() throws Exception {
        reset();
        author = user("작성자", null);
        reindexService.rebuildAll();
        jdbc.execute("TRUNCATE search_outbox RESTART IDENTITY");
    }

    private Feed feed(String content) {
        return tx.execute(status -> feedRepository.save(Feed.builder().userId(author).content(content).build()));
    }

    private void update(long id, String content) {
        tx.executeWithoutResult(status -> feedRepository.findById(id).orElseThrow().update(content));
    }

    private void delete(long id) {
        tx.executeWithoutResult(status -> feedRepository.findById(id).orElseThrow().delete());
    }

    private String feedIndex() throws Exception {
        return client.indices().getAlias(a -> a.name(indexManager.alias(SearchType.FEED))).result().keySet().iterator().next();
    }

    private void close() throws Exception {
        String index = feedIndex();
        client.indices().close(c -> c.index(index));
    }

    private void open() throws Exception {
        String index = feedIndex();
        client.indices().open(o -> o.index(index));
        client.cluster().health(h -> h.index(index).waitForStatus(co.elastic.clients.elasticsearch._types.HealthStatus.Yellow));
    }

    private void retryNow() {
        jdbc.update("UPDATE search_outbox SET next_attempt_at = now() WHERE processed_at IS NULL");
    }

    private void drain() {
        while (worker.processBatch() > 0) {
            // 대기열이 빌 때까지 처리한다
        }
    }

    @Test
    @DisplayName("색인 불가 중의 생성·수정·삭제가 복구 뒤 모두 반영된다")
    void recoversAllChangeTypes() throws Exception {
        Feed updated = feed("kafka 옛내용");
        Feed deleted = feed("redis 지울내용");
        drain();

        close();
        Feed created = feed("docker 새내용");
        update(updated.getId(), "nginx 새내용");
        delete(deleted.getId());
        worker.processBatch();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM search_outbox WHERE processed_at IS NULL", Long.class)).isEqualTo(3);

        open();
        retryNow();
        drain();

        assertThat(jdbc.queryForObject("SELECT count(*) FROM search_outbox WHERE processed_at IS NULL", Long.class)).isZero();
        assertThat(search("docker", "feed").ids()).containsExactly(created.getId().intValue());
        assertThat(search("nginx", "feed").ids()).containsExactly(updated.getId().intValue());
        assertThat(search("kafka", "feed").total()).isZero();
        assertThat(search("redis", "feed").total()).isZero();
        assertThat(indexManager.indexedIds(SearchType.FEED)).containsExactlyInAnyOrder(created.getId(), updated.getId());
    }

    @Test
    @DisplayName("[한계] 장애가 길어져 재시도 한도를 넘긴 변경은 복구 뒤에도 자동으로 반영되지 않는다 → 인프라 장애는 재시도 횟수에서 제외 필요")
    void recoversAfterRetryLimit() throws Exception {
        Feed target = feed("kafka 옛내용");
        drain();

        close();
        update(target.getId(), "nginx 새내용");
        for (int i = 0; i < SearchOutboxWorker.MAX_ATTEMPTS; i++) {
            retryNow();
            worker.processBatch();
        }
        open();
        retryNow();
        drain();

        assertThat(search("nginx", "feed").total()).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM search_outbox WHERE processed_at IS NULL", Long.class)).isEqualTo(1);
    }

    @Test
    @DisplayName("[한계] 정합성 점검은 문서 유무만 비교해서 내용이 뒤처진 문서를 찾지 못한다 → 내용 해시나 수정 시각 비교 필요")
    void reconcileDetectsStaleContent() throws Exception {
        Feed target = feed("kafka 옛내용");
        drain();
        // 대기열 없이 원본만 바뀐 상태(재시도 한도 초과 등)
        jdbc.update("UPDATE feeds SET content = 'nginx 새내용' WHERE id = ?", target.getId());

        reindexService.reconcile();
        drain();

        assertThat(search("nginx", "feed").total()).isZero();
    }

    @Test
    @DisplayName("재시도 한도를 넘긴 변경은 운영 API의 재시도로 반영된다")
    void adminRetryRecovers() throws Exception {
        Feed target = feed("kafka 옛내용");
        drain();

        close();
        update(target.getId(), "nginx 새내용");
        for (int i = 0; i < SearchOutboxWorker.MAX_ATTEMPTS; i++) {
            retryNow();
            worker.processBatch();
        }
        open();
        outbox.retryDead(SearchOutboxWorker.MAX_ATTEMPTS);
        drain();

        assertThat(search("nginx", "feed").ids()).containsExactly(target.getId().intValue());
    }
}
