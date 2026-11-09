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
    @DisplayName("페이지를 넘기는 중 새 글이 들어와도 window를 넘는 구간에서 중복·누락이 없다")
    void insertDuringTraversalBeyondWindow() throws Exception {
        seed(DOCS, "kafka");
        Page first = search(null, "kafka", "post", 0, 20);
        List<Integer> all = new ArrayList<>(first.ids());
        for (int page = 1; page < 9; page++) {
            all.addAll(search(null, "kafka", "post", page, 20, "searchId", first.searchId()).ids());
        }
        insertAndIndex("kafka 새 글");
        for (int page = 9; page < 14; page++) {
            all.addAll(search(null, "kafka", "post", page, 20, "searchId", first.searchId()).ids());
        }
        assertThat(all).hasSize(DOCS).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("최신순도 페이지를 넘기는 중 새 글이 들어오면 searchId로 순서를 유지한다")
    void insertDuringLatestTraversal() throws Exception {
        seed(60, "kafka");
        Page first = search(null, "kafka", "post", 0, 20, "sort", "latest");
        List<Integer> all = new ArrayList<>(first.ids());
        Thread.sleep(50);
        insertAndIndex("kafka 새 글");
        for (int page = 1; page < 3; page++) {
            all.addAll(search(null, "kafka", "post", page, 20, "sort", "latest", "searchId", first.searchId()).ids());
        }
        assertThat(all).hasSize(60).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("다른 비로그인 사용자의 첫 페이지 요청이 와도 searchId로 내 순서가 유지된다")
    void searchIdKeepsOrderAgainstOtherUsers() throws Exception {
        seed(100, "kafka");
        Page first = search(null, "kafka", "post", 0, 20);
        List<Integer> all = new ArrayList<>(first.ids());
        jdbc.update("UPDATE posts SET like_count = 100000 WHERE id IN (SELECT id FROM posts ORDER BY like_count LIMIT 30)");
        search(null, "kafka", "post", 0, 20);
        for (int page = 1; page < 5; page++) {
            all.addAll(search(null, "kafka", "post", page, 20, "searchId", first.searchId()).ids());
        }
        assertThat(all).hasSize(100).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("다른 사용자나 다른 검색어의 searchId는 쓰이지 않는다")
    void searchIdIsBoundToSearch() throws Exception {
        seed(40, "kafka");
        Page first = search(null, "kafka", "post", 0, 20);
        long viewer = user("다른사용자", null);

        assertThat(search(viewer, "kafka", "post", 0, 20, "searchId", first.searchId()).searchId()).isNotEqualTo(first.searchId());
        assertThat(search(null, "본문", "post", 0, 20, "searchId", first.searchId()).searchId()).isNotEqualTo(first.searchId());
    }

    @Test
    @DisplayName("[한계] searchId 없이 넘기면 비로그인끼리 캐시를 공유해서 순서가 바뀔 수 있다 → 이전 방식 호출에만 해당")
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
    @DisplayName("60초 캐시가 만료되어도 searchId로 이어서 넘기면 중복·누락이 없다")
    void cacheExpiryDuringPaging() throws Exception {
        seed(100, "kafka");
        Page first = search(null, "kafka", "post", 0, 20);
        List<Integer> all = new ArrayList<>(first.ids());
        all.addAll(search(null, "kafka", "post", 1, 20, "searchId", first.searchId()).ids());
        jdbc.update("UPDATE posts SET like_count = 100000 WHERE id IN (SELECT id FROM posts ORDER BY like_count LIMIT 30)");
        engine.invalidateViewer(null);
        for (int page = 2; page < 5; page++) {
            all.addAll(search(null, "kafka", "post", page, 20, "searchId", first.searchId()).ids());
        }
        assertThat(all).hasSize(100).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("10,000건을 넘는 페이지를 요청하면 400이다")
    void beyondMaxResultWindow() throws Exception {
        seed(30, "kafka");
        Page deep = search(null, "kafka", "post", 500, 20);
        assertThat(deep.status()).isEqualTo(400);
    }

    @Test
    @DisplayName("캐시된 페이지에서 글이 지워져도 페이지 크기와 총개수가 맞다")
    void deletionKeepsPageSize() throws Exception {
        seed(60, "kafka");
        search(null, "kafka", "post", 0, 20);
        List<Integer> second = search(null, "kafka", "post", 1, 20).ids();
        jdbc.update("UPDATE posts SET status = 'DELETED', deleted_at = now() WHERE id = ?", second.get(0).longValue());

        Page after = search(null, "kafka", "post", 1, 20);
        assertThat(after.ids()).hasSize(20).doesNotContain(second.get(0));
        assertThat(after.total()).isEqualTo(59);
    }
}
