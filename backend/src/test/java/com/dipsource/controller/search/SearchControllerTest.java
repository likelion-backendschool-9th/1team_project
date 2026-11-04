package com.dipsource.controller.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoType;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.user.User;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 1단계(LOWER() + LIKE) 통합 검색 테스트. 실제 PostgreSQL 16 컨테이너에서 돈다.
 * [한계]로 시작하는 테스트는 "지금 방식으로는 안 되는 것"을 현재 동작 그대로 고정해 둔 것이다.
 * 다음 단계에서 개선하면 이 테스트들이 깨지고, 기대값을 새 동작으로 바꾸게 된다.
 */
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test"
})
class SearchControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager em;
    @Autowired
    TransactionTemplate tx;
    @Autowired
    JdbcTemplate jdbc;

    User author;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE posts, feeds, demos, users RESTART IDENTITY CASCADE");
        author = user("작성자", "글 쓰는 사람", List.of());
    }

    @Nested
    @DisplayName("블로그 - 정상 동작")
    class PostNormal {

        @Test
        @DisplayName("제목의 정확한 단어로 찾는다")
        void exactWordInTitle() throws Exception {
            savePost("Spring Security 입문", "본문");
            savePost("React 상태 관리", "본문");

            search("Spring")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("Spring Security 입문"))
                    .andExpect(jsonPath("$.data.content[0].author").value("작성자"))
                    .andExpect(jsonPath("$.data.content[0].price").value(0));
        }

        @Test
        @DisplayName("부분 문자열로 찾는다")
        void substring() throws Exception {
            savePost("Spring Security 입문", "본문");

            search("ecur").andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @ParameterizedTest(name = "q={0}")
        @ValueSource(strings = {"spring", "SPRING", "sPrInG"})
        @DisplayName("영어 대소문자를 구분하지 않는다")
        void ignoreCase(String q) throws Exception {
            savePost("Spring Security 입문", "본문");

            search(q).andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("본문에만 있는 단어로도 찾는다")
        void matchContent() throws Exception {
            savePost("제목", "WebSocket 재연결 로직 정리");

            search("websocket").andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("한글로 찾고, 조사가 붙은 단어도 부분 일치로 찾는다")
        void korean() throws Exception {
            savePost("리팩토링 후기", "레이어를 줄였다");
            savePost("다른 글", "스프링을 처음 배울 때");

            search("리팩토링").andExpect(jsonPath("$.data.totalElements").value(1));
            search("스프링").andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("검색어 앞뒤 공백은 무시한다")
        void trim() throws Exception {
            savePost("Spring Security 입문", "본문");

            search("  spring  ").andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("결과가 없으면 200과 빈 목록")
        void noResult() throws Exception {
            savePost("Spring Security 입문", "본문");

            search("없는키워드xyz")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(0)))
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.totalPages").value(0));
        }

        @Test
        @DisplayName("최신 글이 먼저 나온다")
        void latestFirst() throws Exception {
            savePost("Spring 첫 글", "본문");
            savePost("Spring 두 번째 글", "본문");

            search("spring").andExpect(jsonPath("$.data.content[*].title", contains("Spring 두 번째 글", "Spring 첫 글")));
        }

        @Test
        @DisplayName("미리보기는 공백을 정리하고 120자에서 자른다")
        void excerpt() throws Exception {
            savePost("Spring", "첫 줄\n\n둘째   줄 " + "가".repeat(200));

            String expected = ("첫 줄 둘째 줄 " + "가".repeat(200)).substring(0, 120) + "…";
            search("spring").andExpect(jsonPath("$.data.content[0].excerpt").value(expected));
        }

        @Test
        @DisplayName("로그인하지 않아도 검색할 수 있다")
        void guest() throws Exception {
            search("spring").andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("LIKE 특수문자와 SQL Injection")
    class SpecialCharacters {

        @Test
        @DisplayName("%는 와일드카드가 아니라 글자 그대로 비교한다")
        void percent() throws Exception {
            savePost("목표 100% 달성", "본문");
            savePost("목표 1000건 달성", "본문");

            search("100%")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("목표 100% 달성"));
        }

        @Test
        @DisplayName("_는 한 글자 와일드카드가 아니라 글자 그대로 비교한다")
        void underscore() throws Exception {
            savePost("user_id 컬럼", "본문");
            savePost("userXid 컬럼", "본문");

            search("user_id")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("user_id 컬럼"));
        }

        @Test
        @DisplayName("역슬래시도 글자 그대로 비교한다")
        void backslash() throws Exception {
            savePost("C:\\temp 경로 문제", "본문");
            savePost("C:temp 경로 문제", "본문");

            search("C:\\temp").andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("% 하나만 검색해도 전체가 나오지 않는다 (모든 대상)")
        void onlyPercent() throws Exception {
            savePost("Spring", "본문");
            saveFeed("Spring 피드");
            saveDemo("Spring 데모", "설명");

            search("%", "all")
                    .andExpect(jsonPath("$.data.posts.totalElements").value(0))
                    .andExpect(jsonPath("$.data.feeds.totalElements").value(0))
                    .andExpect(jsonPath("$.data.demos.totalElements").value(0))
                    .andExpect(jsonPath("$.data.users.totalElements").value(0));
        }

        @ParameterizedTest(name = "q={0}")
        @ValueSource(strings = {"' OR 1=1 --", "'; DROP TABLE posts; --"})
        @DisplayName("SQL Injection 문자열은 그냥 검색어로 취급된다")
        void sqlInjection(String q) throws Exception {
            savePost("Spring", "본문");

            search(q, "all").andExpect(status().isOk()).andExpect(jsonPath("$.data.posts.totalElements").value(0));
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM posts", Long.class)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("페이지네이션")
    class Pagination {

        @BeforeEach
        void save25() {
            for (int i = 1; i <= 25; i++) {
                savePost("Spring 글 " + i, "본문");
            }
        }

        @Test
        @DisplayName("page=1, size=10이면 11~20번째 글")
        void secondPage() throws Exception {
            search("spring", "post", "page", "1", "size", "10")
                    .andExpect(jsonPath("$.data.content", hasSize(10)))
                    .andExpect(jsonPath("$.data.content[0].title").value("Spring 글 15"))
                    .andExpect(jsonPath("$.data.page").value(1))
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.totalElements").value(25))
                    .andExpect(jsonPath("$.data.totalPages").value(3));
        }

        @Test
        @DisplayName("마지막 페이지는 남은 개수만큼")
        void lastPage() throws Exception {
            search("spring", "post", "page", "2", "size", "10").andExpect(jsonPath("$.data.content", hasSize(5)));
        }

        @Test
        @DisplayName("범위를 넘은 페이지는 빈 목록")
        void outOfRange() throws Exception {
            search("spring", "post", "page", "9", "size", "10")
                    .andExpect(jsonPath("$.data.content", hasSize(0)))
                    .andExpect(jsonPath("$.data.totalElements").value(25));
        }

        @Test
        @DisplayName("size는 최대 50으로 줄인다")
        void maxSize() throws Exception {
            search("spring", "post", "size", "1000").andExpect(jsonPath("$.data.size").value(50));
        }
    }

    @Nested
    @DisplayName("블로그 - 공개 범위와 유료 글")
    class PostVisibility {

        @Test
        @DisplayName("숨김·삭제된 글은 나오지 않는다")
        void hiddenAndDeleted() throws Exception {
            savePost("Spring 공개", "본문");
            Post hidden = post("Spring 숨김", "본문", false);
            hidden.hide();
            persist(hidden);
            Post deleted = post("Spring 삭제", "본문", false);
            deleted.delete();
            persist(deleted);

            search("spring")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("Spring 공개"));
        }

        @Test
        @DisplayName("유료 글은 본문으로 검색되지 않는다")
        void paidContentNotSearchable() throws Exception {
            persist(post("유료 강의", "비밀 노하우 Kafka 튜닝", true));

            search("kafka").andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("유료 글은 제목으로 검색되지만 미리보기는 주지 않는다")
        void paidTitleOnly() throws Exception {
            persist(post("Kafka 튜닝 유료 강의", "비밀 노하우", true));

            search("kafka")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].excerpt").doesNotExist())
                    .andExpect(jsonPath("$.data.content[0].price").value(30));
        }

        @Test
        @DisplayName("정지·탈퇴 회원의 공개 글은 나오고, 탈퇴 회원은 '삭제된 유저'로 보인다")
        void inactiveAuthorsPost() throws Exception {
            User suspended = user("정지회원", null, List.of());
            jdbc.update("UPDATE users SET status = 'SUSPENDED' WHERE id = ?", suspended.getId());
            User withdrawn = user("탈퇴회원", null, List.of());
            jdbc.update("UPDATE users SET status = 'DELETED' WHERE id = ?", withdrawn.getId());
            persist(Post.builder().userId(suspended.getId()).title("Spring 정지").content("본문").build());
            persist(Post.builder().userId(withdrawn.getId()).title("Spring 탈퇴").content("본문").build());

            search("spring")
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.content[0].author").value(User.WITHDRAWN_NICKNAME))
                    .andExpect(jsonPath("$.data.content[1].author").value("정지회원"));
        }
    }

    @Nested
    @DisplayName("피드")
    class FeedSearch {

        @Test
        @DisplayName("본문으로 찾고, 대소문자를 구분하지 않는다")
        void content() throws Exception {
            saveFeed("Spring Boot 3.4 업데이트 정리");
            saveFeed("React 19 출시");

            search("SPRING", "feed")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].content").value("Spring Boot 3.4 업데이트 정리"))
                    .andExpect(jsonPath("$.data.content[0].author").value("작성자"));
        }

        @Test
        @DisplayName("숨김·삭제된 피드는 나오지 않는다")
        void hiddenAndDeleted() throws Exception {
            saveFeed("Spring 공개 피드");
            Feed hidden = feed("Spring 숨김 피드");
            hidden.hide();
            persist(hidden);
            Feed deleted = feed("Spring 삭제 피드");
            deleted.delete();
            persist(deleted);

            search("spring", "feed")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].content").value("Spring 공개 피드"));
        }
    }

    @Nested
    @DisplayName("데모")
    class DemoSearch {

        @Test
        @DisplayName("제목 또는 설명으로 찾는다")
        void titleOrDescription() throws Exception {
            saveDemo("실시간 채팅앱", "WebSocket + STOMP");
            saveDemo("오목 게임", "Unity WebGL");
            saveDemo("설명 없는 데모", null);

            search("websocket", "demo")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("실시간 채팅앱"))
                    .andExpect(jsonPath("$.data.content[0].demoType").value("STATIC"));
            search("오목", "demo").andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("숨김·삭제된 데모는 나오지 않는다")
        void hiddenAndDeleted() throws Exception {
            saveDemo("Spring 공개 데모", null);
            Demo hidden = demo("Spring 숨김 데모", null);
            hidden.hide();
            persist(hidden);
            Demo deleted = demo("Spring 삭제 데모", null);
            deleted.delete();
            persist(deleted);

            search("spring", "demo")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("Spring 공개 데모"));
        }
    }

    @Nested
    @DisplayName("사람")
    class UserSearch {

        @Test
        @DisplayName("닉네임, 자기소개, 기술 스택으로 찾는다")
        void fields() throws Exception {
            user("reactqueen", "프론트엔드 개발자", List.of("React"));
            user("데이터곰", "검색 엔진에 관심 많음", List.of("Kafka", "Python"));

            search("REACT", "user")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].nickname").value("reactqueen"));
            search("검색 엔진", "user").andExpect(jsonPath("$.data.totalElements").value(1));
            search("kafka", "user")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].techStacks", contains("Kafka", "Python")));
        }

        @Test
        @DisplayName("정지·탈퇴 회원은 나오지 않는다")
        void inactiveUsers() throws Exception {
            user("spring_active", null, List.of());
            User suspended = user("spring_suspended", null, List.of());
            jdbc.update("UPDATE users SET status = 'SUSPENDED' WHERE id = ?", suspended.getId());
            User withdrawn = user("spring_withdrawn", null, List.of());
            jdbc.update("UPDATE users SET status = 'DELETED' WHERE id = ?", withdrawn.getId());

            search("spring_", "user")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].nickname").value("spring_active"));
        }
    }

    @Nested
    @DisplayName("전체 검색 (type=all)")
    class All {

        @Test
        @DisplayName("type을 보내지 않으면 전체 검색이고, 대상별 결과와 총개수를 준다")
        void defaultIsAll() throws Exception {
            savePost("Spring 블로그", "본문");
            saveFeed("Spring 피드");
            saveDemo("Spring 데모", null);
            user("spring_user", null, List.of());

            mockMvc.perform(get("/api/v1/search").param("q", "spring"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.posts.totalElements").value(1))
                    .andExpect(jsonPath("$.data.posts.content[0].title").value("Spring 블로그"))
                    .andExpect(jsonPath("$.data.feeds.totalElements").value(1))
                    .andExpect(jsonPath("$.data.demos.totalElements").value(1))
                    .andExpect(jsonPath("$.data.users.totalElements").value(1))
                    .andExpect(jsonPath("$.data.users.content[0].nickname").value("spring_user"));
        }

        @Test
        @DisplayName("대상별로 최대 5건만 미리 보여 주고, 총개수는 전체 건수")
        void previewSize() throws Exception {
            for (int i = 1; i <= 7; i++) {
                savePost("Spring 글 " + i, "본문");
            }

            search("spring", "all")
                    .andExpect(jsonPath("$.data.posts.content", hasSize(5)))
                    .andExpect(jsonPath("$.data.posts.content[0].title").value("Spring 글 7"))
                    .andExpect(jsonPath("$.data.posts.totalElements").value(7));
        }

        @Test
        @DisplayName("결과가 없는 대상은 빈 목록과 0")
        void emptySection() throws Exception {
            savePost("Spring 블로그", "본문");

            search("spring", "all")
                    .andExpect(jsonPath("$.data.posts.totalElements").value(1))
                    .andExpect(jsonPath("$.data.feeds.content", hasSize(0)))
                    .andExpect(jsonPath("$.data.feeds.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("잘못된 요청")
    class BadRequest {

        @Test
        @DisplayName("검색어가 없으면 400")
        void missingQuery() throws Exception {
            mockMvc.perform(get("/api/v1/search").param("type", "post"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
        }

        @ParameterizedTest(name = "q=[{0}]")
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("빈 값이나 공백만 있으면 400 (전체 검색도 같음)")
        void blankQuery(String q) throws Exception {
            search(q).andExpect(status().isBadRequest());
            search(q, "all").andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("50자는 허용하고 51자는 400")
        void queryLength() throws Exception {
            search("가".repeat(50)).andExpect(status().isOk());
            search("가".repeat(51)).andExpect(status().isBadRequest());
        }

        @ParameterizedTest(name = "type={0}")
        @ValueSource(strings = {"", "users", "POST", "posts", "blog"})
        @DisplayName("지원하지 않는 type이면 400")
        void unsupportedType(String type) throws Exception {
            search("spring", type).andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("[한계] LIKE 검색으로는 안 되는 것")
    class Limitations {

        @Test
        @DisplayName("[한계] 오타(Sprng)는 Spring 글을 찾지 못한다 → 3단계")
        void typo() throws Exception {
            savePost("Spring Security 입문", "본문");

            search("Sprng").andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("[한계] java로 검색하면 JavaScript 글도 섞인다(단어 경계 없음) → 7단계")
        void noWordBoundary() throws Exception {
            savePost("Java 21 가상 스레드", "본문");
            savePost("JavaScript 클로저", "본문");

            search("java").andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("[한계] 'boot spring'은 'Spring Boot' 글을 찾지 못한다(문장 전체를 한 덩어리로 비교) → 2단계")
        void wordOrder() throws Exception {
            savePost("Spring Boot 시작하기", "본문");

            search("spring boot").andExpect(jsonPath("$.data.totalElements").value(1));
            search("boot spring").andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("[한계] 제목에 있는 글보다 본문에 한 번 나온 최신 글이 먼저 나온다(관련도 없음) → 2단계")
        void noRelevance() throws Exception {
            savePost("Redis 캐시 전략 총정리", "본문");
            savePost("오늘의 회고", "점심 먹고 redis 잠깐 봤다");

            search("redis").andExpect(jsonPath("$.data.content[*].title", contains("오늘의 회고", "Redis 캐시 전략 총정리")));
        }

        @Test
        @DisplayName("[한계] '스프링'으로 'Spring' 글을 찾지 못한다(의미·동의어) → 9단계")
        void noSynonym() throws Exception {
            savePost("Spring Security 입문", "본문");

            search("스프링").andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("[한계] 기술 스택 jsonb를 문자열로 비교해서 따옴표·쉼표까지 매칭된다 → 태그 기반 검색으로 개선")
        void techStacksAsText() throws Exception {
            user("아무개", null, List.of("Java", "Spring"));

            // jsonb는 문자열로 바꾸면 ["Java", "Spring"] 형태가 된다
            search("a\", \"S", "user").andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }

    /** 블로그 검색 요청 (기존 테스트 호환) */
    private ResultActions search(String q) throws Exception {
        return search(q, "post");
    }

    private ResultActions search(String q, String type, String... extraParams) throws Exception {
        var request = get("/api/v1/search").param("q", q).param("type", type);
        for (int i = 0; i < extraParams.length; i += 2) {
            request.param(extraParams[i], extraParams[i + 1]);
        }
        return mockMvc.perform(request);
    }

    private <T> T persist(T entity) {
        return tx.execute(status -> {
            em.persist(entity);
            return entity;
        });
    }

    private User user(String nickname, String bio, List<String> techStacks) {
        return persist(User.builder()
                .email(nickname + "@test.local")
                .nickname(nickname)
                .bio(bio)
                .techStacks(techStacks)
                .build());
    }

    private void savePost(String title, String content) {
        persist(post(title, content, false));
    }

    private Post post(String title, String content, boolean paid) {
        return Post.builder()
                .userId(author.getId())
                .title(title)
                .content(content)
                .paid(paid)
                .priceChips(paid ? 30 : 0)
                .build();
    }

    private void saveFeed(String content) {
        persist(feed(content));
    }

    private Feed feed(String content) {
        return Feed.builder().userId(author.getId()).content(content).build();
    }

    private void saveDemo(String title, String description) {
        persist(demo(title, description));
    }

    private Demo demo(String title, String description) {
        return Demo.builder().userId(author.getId()).title(title).description(description).demoType(DemoType.STATIC).build();
    }
}
