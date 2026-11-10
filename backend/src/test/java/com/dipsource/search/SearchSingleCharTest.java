package com.dipsource.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.dipsource.service.search.sync.SearchReindexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** 한 글자 검색은 허용 목록의 용어만, 태그·독립 단어·정의된 구문으로만 찾는다. 실제 Elasticsearch에서 돈다. */
@AutoConfigureMockMvc
@SpringBootTest(properties = {AbstractSearchVerification.JWT, AbstractSearchVerification.GITHUB_ID, AbstractSearchVerification.GITHUB_SECRET,
        "search.elasticsearch.enabled=true", "search.elasticsearch.index-prefix=singlechartest",
        "search.elasticsearch.bootstrap-on-empty=false", "search.engine=elasticsearch",
        "search.fallback-to-postgres=false", "search.sync.poll-interval=PT1H"})
class SearchSingleCharTest extends AbstractSearchVerification {

    @DynamicPropertySource
    static void elasticsearch(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", SearchContainers::elasticsearchUri);
    }

    @Autowired
    SearchReindexService reindexService;

    long author;

    @BeforeEach
    void setUp() {
        reset();
        author = user("작성자", null);
    }

    private void tag(long postId, String name) {
        long tagId = jdbc.queryForObject("""
                INSERT INTO tags(name) VALUES (?) ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name RETURNING id
                """, Long.class, name);
        jdbc.update("INSERT INTO post_tags(post_id, tag_id) VALUES (?, ?)", postId, tagId);
    }

    private long tagged(String title, String content, String tagName) {
        long id = post(author, title, content);
        tag(id, tagName);
        return id;
    }

    private void reindex() throws Exception {
        reindexService.rebuildAll();
    }

    @ParameterizedTest(name = "q={0}")
    @ValueSource(strings = {"C", "c", "R", "D", "V", "J", "q", "Q", "웹", "앱", "봇", "깃"})
    @DisplayName("허용 목록의 한 글자는 검색할 수 있다")
    void allowed(String q) throws Exception {
        reindex();
        assertThat(search(q, "post").status()).isEqualTo(200);
    }

    @ParameterizedTest(name = "q={0}")
    @ValueSource(strings = {"K", "B", "X", "a", "스", "뷰", "F"})
    @DisplayName("허용 목록에 없는 한 글자도 일반 검색으로 처리한다")
    void otherSingleCharacters(String q) throws Exception {
        reindex();
        assertThat(search(q, "post").status()).isEqualTo(200);
    }

    @Test
    @DisplayName("허용 목록에 없는 한 글자는 결과가 없으면 그 글자로 시작하는 단어를 찾는다")
    void singleCharacterPrefixFallback() throws Exception {
        long id = post(author, "스프링 입문", "내용");
        reindex();

        assertThat(search("스", "post").ids()).containsExactly((int) id);
    }

    @Test
    @DisplayName("C, C++, C#, F#를 서로 구분한다")
    void symbolVariants() throws Exception {
        long c = tagged("C 언어 포인터", "메모리", "C");
        long cpp = tagged("C++ 템플릿", "메타프로그래밍", "C++");
        long cs = tagged("C# LINQ", "지연 실행", "C#");
        long fs = tagged("F# 입문", "파이프", "F#");
        reindex();

        assertThat(search("C", "post").ids()).containsExactly((int) c);
        assertThat(search("C++", "post").ids()).containsExactly((int) cpp);
        assertThat(search("C#", "post").ids()).containsExactly((int) cs);
        assertThat(search("F#", "post").ids()).containsExactly((int) fs);
    }

    @Test
    @DisplayName("영문 한 글자는 일반 본문과 제목의 낱글자에 걸리지 않는다")
    void latinIsNotFullText() throws Exception {
        long tagOnly = tagged("메모리 배치 이야기", "구조체 패딩", "C");
        long phrase = post(author, "포인터 정리", "C 언어에서 주소를 다룬다");
        post(author, "C 레벨 임원 보고", "C 등급 평가 사례");
        post(author, "abc 정리", "c 라는 글자가 들어 있다");
        reindex();

        assertThat(search("C", "post").ids()).containsExactlyInAnyOrder((int) tagOnly, (int) phrase);
    }

    @Test
    @DisplayName("웹은 태그와 독립 단어만 찾고 웹소켓·웹사이트·본문 언급은 뺀다")
    void koreanNoun() throws Exception {
        long tagOnly = tagged("접근성 정리", "스크린 리더", "웹");
        long title = post(author, "웹 개발 입문", "내용");
        post(author, "웹소켓 재연결", "내용");
        post(author, "웹사이트 이전", "내용");
        post(author, "프론트엔드 이야기", "웹 관련 내용이 본문에만 있다");
        reindex();

        assertThat(search("웹", "post").ids()).containsExactlyInAnyOrder((int) tagOnly, (int) title);
    }

    @Test
    @DisplayName("깃은 git 태그·제목을 찾고 깃발은 뺀다")
    void git() throws Exception {
        long ko = tagged("깃 브랜치 전략", "내용", "Git");
        long en = post(author, "Git rebase 정리", "내용");
        post(author, "깃발 디자인", "내용");
        reindex();

        assertThat(search("깃", "post").ids()).containsExactlyInAnyOrder((int) ko, (int) en);
    }

    @Test
    @DisplayName("한 글자로도 사용자 기술 스택과 닉네임 전체 일치를 찾는다")
    void users() throws Exception {
        long stack = jdbc.queryForObject("""
                INSERT INTO users(email, nickname, tech_stacks, role, status, chip_balance, blog_subscription_price, created_at, updated_at)
                VALUES ('s@test.local', '시스템개발자', '["C"]', 'USER', 'ACTIVE', 0, 0, now(), now()) RETURNING id
                """, Long.class);
        user("c", "닉네임이 c");
        user("abc개발자", "c 를 좋아함");
        reindex();

        assertThat(search("C", "user").ids()).hasSize(2).contains((int) stack);
    }

    @Test
    @DisplayName("다른 단어와 함께 쓰면 일반 검색으로 동작한다")
    void withOtherWords() throws Exception {
        long id = post(author, "포인터 정리", "C 포인터 예제");
        reindex();

        assertThat(search("C 포인터", "post").ids()).containsExactly((int) id);
    }
}
