package com.dipsource.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/** Elasticsearch에 연결할 수 없을 때 PostgreSQL 대체 경로의 노출 정책. */
@AutoConfigureMockMvc
@SpringBootTest(properties = {AbstractSearchVerification.JWT, AbstractSearchVerification.GITHUB_ID, AbstractSearchVerification.GITHUB_SECRET,
        "search.elasticsearch.enabled=true", "search.elasticsearch.bootstrap-on-empty=false", "search.engine=elasticsearch",
        "search.sync.poll-interval=PT1H", "spring.elasticsearch.uris=http://localhost:1", "spring.elasticsearch.connection-timeout=200ms"})
class SearchOutageVerificationTest extends AbstractSearchVerification {

    long author;
    long viewer;

    @BeforeEach
    void setUp() {
        reset();
        author = user("작성자", null);
        viewer = user("검색자", null);
    }

    @Test
    @DisplayName("장애 중에도 삭제·숨김 글과 유료 본문은 노출되지 않는다")
    void statusAndPaid() throws Exception {
        long visible = post(author, "Kafka 공개", "본문");
        long deleted = post(author, "Kafka 삭제", "본문");
        jdbc.update("UPDATE posts SET status = 'DELETED', deleted_at = now() WHERE id = ?", deleted);
        long hidden = post(author, "Kafka 숨김", "본문");
        jdbc.update("UPDATE posts SET status = 'HIDDEN' WHERE id = ?", hidden);
        long paid = post(author, "다른 제목", "유료비밀단어");
        jdbc.update("UPDATE posts SET is_paid = true, price_chips = 10 WHERE id = ?", paid);

        assertThat(search("kafka", "post").ids()).containsExactly((int) visible);
        assertThat(search("유료비밀단어", "post").ids()).doesNotContain((int) paid);
    }

    @Test
    @DisplayName("장애 중에도 차단 작성자와 숨긴 결과는 빠진다")
    void blockAndHidden() throws Exception {
        long blockedAuthor = user("차단대상", null);
        long blockedPost = post(blockedAuthor, "Kafka 차단 글", "본문");
        long hiddenPost = post(author, "Kafka 숨긴 글", "본문");
        long normal = post(author, "Kafka 일반 글", "본문");
        block(viewer, blockedAuthor);
        jdbc.update("INSERT INTO search_hidden_results(user_id, target_type, target_id, created_at) VALUES (?, 'POST', ?, now())", viewer, hiddenPost);

        var ids = search(viewer, "kafka", "post", 0, 20).ids();
        assertThat(ids).contains((int) normal).doesNotContain((int) blockedPost, (int) hiddenPost);
    }

    @Test
    @DisplayName("장애 중 총개수도 차단·숨김을 뺀 값이다")
    void totalExcludesBlocked() throws Exception {
        long blockedAuthor = user("차단대상", null);
        post(blockedAuthor, "Kafka 차단 글", "본문");
        post(author, "Kafka 일반 글", "본문");
        block(viewer, blockedAuthor);

        Page page = search(viewer, "kafka", "post", 0, 20);
        assertThat(page.ids()).hasSize(1);
        assertThat(page.total()).isEqualTo(1);
    }

    @Test
    @DisplayName("장애 중 한 글자 검색은 부분 문자열로 전부 찾지 않고 빈 결과를 준다")
    void singleCharacterRule() throws Exception {
        post(author, "Kafka 글", "본문");
        search("kafka", "post");

        Page page = search("a", "post");
        assertThat(page.status()).isEqualTo(200);
        assertThat(page.total()).isZero();
    }

    @Test
    @DisplayName("장애 중 허용된 한 글자는 부분 문자열로 찾지 않고 빈 결과를 준다")
    void allowedSingleCharacterIsNotSubstring() throws Exception {
        post(author, "Kafka 글", "본문 c 포함");
        search("kafka", "post");

        Page page = search("C", "post");
        assertThat(page.status()).isEqualTo(200);
        assertThat(page.total()).isZero();
    }
}
