package com.dipsource.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.dipsource.service.search.impl.EngineSearchService;
import com.dipsource.service.search.sync.SearchOutboxRepository;
import com.dipsource.service.search.sync.SearchOutboxWorker;
import com.dipsource.service.search.sync.SearchReindexService;
import com.dipsource.dto.search.SearchType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** 상위 200건 재정렬과 60초 캐시 구조의 깊은 페이지, 중복·누락, 순서 변경 검증. 기본 설정(window 200, TTL 60초)으로 돈다. */
@AutoConfigureMockMvc
@SpringBootTest(properties = {AbstractSearchVerification.JWT, AbstractSearchVerification.GITHUB_ID, AbstractSearchVerification.GITHUB_SECRET,
        "search.elasticsearch.enabled=true", "search.elasticsearch.index-prefix=pagetest",
        "search.elasticsearch.bootstrap-on-empty=false", "search.engine=elasticsearch",
        "search.fallback-to-postgres=false", "search.sync.poll-interval=PT1H"})
class SearchPaginationVerificationTest extends AbstractSearchVerification {

    @DynamicPropertySource
    static void elasticsearch(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", SearchContainers::elasticsearchUri);
    }

    private static final int DOCS = 260;

    @Autowired
    SearchReindexService reindexService;
    @Autowired
    SearchOutboxRepository outbox;
    @Autowired
    SearchOutboxWorker worker;
    @Autowired
    EngineSearchService engine;

    long author;

    @BeforeEach
    void setUp() {
        reset();
        author = user("작성자", null);
    }

    private void seed(int count, String word) throws Exception {
        for (int i = 0; i < count; i++) {
            long id = post(author, word + " 글 " + i, "본문 " + i);
            jdbc.update("UPDATE posts SET like_count = ? WHERE id = ?", i * 13 % 97, id);
        }
        reindexService.rebuildAll();
        jdbc.execute("TRUNCATE search_outbox RESTART IDENTITY");
        engine.invalidateViewer(null);
    }

    private List<Integer> traverse(String q, int size) throws Exception {
        List<Integer> all = new ArrayList<>();
        Page first = search(null, q, "post", 0, size);
        all.addAll(first.ids());
        for (int page = 1; page < first.totalPages(); page++) {
            all.addAll(search(null, q, "post", page, size).ids());
        }
        return all;
    }

    private void insertAndIndex(String title) {
        long id = post(author, title, "본문");
        outbox.enqueue(SearchType.POST, List.of(id));
        while (worker.processBatch() > 0) {
            // 색인 반영
        }
    }

    @Test
    @DisplayName("변경이 없으면 window 경계를 지나 끝까지 넘겨도 중복·누락이 없다")
    void fullTraversal() throws Exception {
        seed(DOCS, "kafka");
        assertThat(traverse("kafka", 20)).hasSize(DOCS).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("한 페이지가 window 경계에 걸려도 중복·누락이 없다")
    void pageCrossingWindowBoundary() throws Exception {
        seed(DOCS, "kafka");
        assertThat(traverse("kafka", 30)).hasSize(DOCS).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("깊은 페이지로 바로 들어가도 순서대로 넘긴 결과와 같다")
    void directDeepPage() throws Exception {
        seed(DOCS, "kafka");
        List<Integer> sequential = traverse("kafka", 20);
        engine.invalidateViewer(null);

        assertThat(search(null, "kafka", "post", 11, 20).ids()).isEqualTo(sequential.subList(220, 240));
    }

    @Test
    @DisplayName("최신순도 끝까지 중복·누락이 없다")
    void latestTraversal() throws Exception {
        seed(DOCS, "kafka");
        List<Integer> all = new ArrayList<>();
        for (int page = 0; page < 13; page++) {
            all.addAll(search(null, "kafka", "post", page, 20, "sort", "latest").ids());
        }
        assertThat(all).hasSize(DOCS).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("재정렬 범위 밖의 문서는 인기가 많아도 올라오지 않는다")
    void outsideWindowIsNotReranked() throws Exception {
        for (int i = 0; i < 210; i++) {
            post(author, "kafka kafka 집중 글 " + i, "kafka 본문");
        }
        long popular = post(author, "여러 주제를 길게 다루는 글입니다 잡담이 많습니다", "kafka 를 한 번 언급하고 다른 이야기를 계속 이어 가는 긴 본문입니다");
        jdbc.update("UPDATE posts SET like_count = 1000000, view_count = 1000000 WHERE id = ?", popular);
        reindexService.rebuildAll();
        engine.invalidateViewer(null);

        assertThat(traverse("kafka", 50).indexOf((int) popular)).isGreaterThanOrEqualTo(200);
    }

    @Test
    @DisplayName("페이지를 넘기는 중 새 글이 들어와도 window 안에서는 중복·누락이 없다")
    void insertDuringTraversalInsideWindow() throws Exception {
        seed(100, "kafka");
        List<Integer> all = new ArrayList<>(search(null, "kafka", "post", 0, 20).ids());
        insertAndIndex("kafka 새 글");
        for (int page = 1; page < 5; page++) {
            all.addAll(search(null, "kafka", "post", page, 20).ids());
        }
        assertThat(all).hasSize(100).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("[한계] window를 넘는 구간은 실시간 조회라 페이지를 넘기는 중 새 글이 들어오면 중복이 생긴다 → PIT·search_after 필요")
    void insertDuringTraversalBeyondWindow() throws Exception {
        seed(DOCS, "kafka");
        List<Integer> all = new ArrayList<>(search(null, "kafka", "post", 0, 20).ids());
        for (int page = 1; page < 9; page++) {
            all.addAll(search(null, "kafka", "post", page, 20).ids());
        }
        insertAndIndex("kafka 새 글");
        for (int page = 9; page < 14; page++) {
            all.addAll(search(null, "kafka", "post", page, 20).ids());
        }
        assertThat(all.stream().distinct().count()).isLessThan(all.size());
    }

    @Test
    @DisplayName("[한계] 비로그인은 캐시를 공유해서 다른 사람의 첫 페이지 요청이 순서를 바꾸면 중복·누락이 생긴다 → 탐색 단위 스냅샷 필요")
    void anonymousFirstPageDoesNotDisturbPaging() throws Exception {
        seed(100, "kafka");
        List<Integer> all = new ArrayList<>(search(null, "kafka", "post", 0, 20).ids());
        // 순위가 낮은 글의 좋아요가 늘어난 뒤 다른 사람이 첫 페이지를 연다
        jdbc.update("UPDATE posts SET like_count = 100000 WHERE id IN (SELECT id FROM posts ORDER BY like_count LIMIT 30)");
        search(null, "kafka", "post", 0, 20);
        for (int page = 1; page < 5; page++) {
            all.addAll(search(null, "kafka", "post", page, 20).ids());
        }
        assertThat(all.stream().distinct().count()).isLessThan(100);
    }

    @Test
    @DisplayName("[한계] 60초 캐시가 만료된 뒤 순위가 바뀌어 있으면 이어서 넘길 때 중복·누락이 생긴다 → 탐색 단위 스냅샷 필요")
    void cacheExpiryDuringPaging() throws Exception {
        seed(100, "kafka");
        List<Integer> all = new ArrayList<>(search(null, "kafka", "post", 0, 20).ids());
        all.addAll(search(null, "kafka", "post", 1, 20).ids());
        jdbc.update("UPDATE posts SET like_count = 100000 WHERE id IN (SELECT id FROM posts ORDER BY like_count LIMIT 30)");
        engine.invalidateViewer(null);
        for (int page = 2; page < 5; page++) {
            all.addAll(search(null, "kafka", "post", page, 20).ids());
        }
        assertThat(all.stream().distinct().count()).isLessThan(100);
    }

    @Test
    @DisplayName("[한계] 10,000건을 넘는 페이지를 요청하면 빈 결과와 함께 총개수가 0이 된다 → 총개수 유지 또는 400 필요")
    void beyondMaxResultWindow() throws Exception {
        seed(30, "kafka");
        Page deep = search(null, "kafka", "post", 500, 20);
        assertThat(deep.status()).isEqualTo(200);
        assertThat(deep.total()).isZero();
    }

    @Test
    @DisplayName("[한계] 캐시된 페이지에서 글이 지워지면 그 페이지만 한 건 짧아진다 → 뒤 문서로 채우는 처리 필요")
    void deletionKeepsPageSize() throws Exception {
        seed(60, "kafka");
        search(null, "kafka", "post", 0, 20);
        List<Integer> second = search(null, "kafka", "post", 1, 20).ids();
        jdbc.update("UPDATE posts SET status = 'DELETED', deleted_at = now() WHERE id = ?", second.get(0).longValue());

        assertThat(search(null, "kafka", "post", 1, 20).ids()).hasSize(19);
    }
}
