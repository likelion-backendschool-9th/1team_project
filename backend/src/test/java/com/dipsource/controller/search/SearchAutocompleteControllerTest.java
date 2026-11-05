package com.dipsource.controller.search;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoType;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.tag.Tag;
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
 * 검색어 자동완성(4단계) 테스트. 실제 PostgreSQL 16 컨테이너에서 돈다.
 * [한계]로 시작하는 테스트는 "지금 방식으로는 안 되는 것"을 현재 동작 그대로 고정해 둔 것이다.
 */
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test"
})
class SearchAutocompleteControllerTest {

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
        jdbc.execute("TRUNCATE posts, feeds, demos, users, tags RESTART IDENTITY CASCADE");
        author = persist(User.builder().email("writer@test.local").nickname("작성자").techStacks(List.of()).build());
    }

    @Nested
    @DisplayName("정상 동작")
    class Normal {

        @Test
        @DisplayName("입력으로 시작하는 단어를 준다 (spr → spring)")
        void prefixWord() throws Exception {
            savePost("Spring Security 입문");

            autocomplete("spr")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].text").value("spring"))
                    .andExpect(jsonPath("$.data[0].type").value("WORD"))
                    // security는 spr로 시작하지 않는다
                    .andExpect(jsonPath("$.data[*].text", not(hasItem("security"))));
        }

        @Test
        @DisplayName("태그가 단어보다 먼저 나온다")
        void tagsFirst() throws Exception {
            tag("Spring Boot");
            savePost("Spring 실전");

            autocomplete("spr")
                    .andExpect(jsonPath("$.data[*].text", contains("Spring Boot", "spring")))
                    .andExpect(jsonPath("$.data[*].type", contains("TAG", "WORD")));
        }

        @Test
        @DisplayName("입력과 똑같은 후보가 태그보다도 먼저 나온다")
        void exactFirst() throws Exception {
            tag("Spring Boot");
            savePost("Spring 실전");

            autocomplete("spring")
                    .andExpect(jsonPath("$.data[*].text", contains("spring", "Spring Boot")));
        }

        @Test
        @DisplayName("단어는 자주 나온 순, 같으면 짧은 순이다")
        void wordOrder() throws Exception {
            savePost("Sprint 회고");
            savePost("Springboot 정리");
            savePost("Spring 입문");
            savePost("Spring 실전");

            autocomplete("spr")
                    .andExpect(jsonPath("$.data[*].text", contains("spring", "sprint", "springboot")));
        }

        @Test
        @DisplayName("등장 횟수는 태그·글 제목·데모 제목을 합쳐서 센다")
        void frequencyAcrossSources() throws Exception {
            savePost("React 상태 관리");
            savePost("Redis 캐시");
            saveDemo("Redis 대시보드");

            autocomplete("re")
                    .andExpect(jsonPath("$.data[*].text", contains("redis", "react")));
        }

        @Test
        @DisplayName("여러 단어를 입력하면 앞 단어는 그대로 두고 마지막 단어만 완성한다")
        void completeLastWord() throws Exception {
            savePost("Spring Batch 정리");
            savePost("Boot 캠프 후기");

            autocomplete("spring b")
                    .andExpect(jsonPath("$.data[*].text", contains("spring boot", "spring batch")));
        }

        @Test
        @DisplayName("태그는 입력 전체로 찾고, 같은 검색어의 단어 후보는 빼서 한 번만 준다")
        void tagPhrase() throws Exception {
            tag("Spring Boot");
            savePost("Boot 캠프 후기");

            autocomplete("spring b")
                    .andExpect(jsonPath("$.data[*].text", contains("Spring Boot")))
                    .andExpect(jsonPath("$.data[0].type").value("TAG"));
        }

        @Test
        @DisplayName("대소문자와 앞뒤·연속 공백은 구분하지 않는다")
        void normalize() throws Exception {
            savePost("Spring Boot 입문");

            autocomplete("  SPRING   bo ")
                    .andExpect(jsonPath("$.data[*].text", contains("spring boot")));
        }

        @Test
        @DisplayName("최대 8개, 태그는 그중 최대 4개다")
        void limits() throws Exception {
            for (int i = 1; i <= 6; i++) {
                tag("Java " + i);
            }
            savePost("javac jar javadoc javafx javaee javalin jakarta jdk");

            autocomplete("ja")
                    .andExpect(jsonPath("$.data", hasSize(8)))
                    .andExpect(jsonPath("$.data[?(@.type == 'TAG')]", hasSize(4)));
        }
    }

    @Nested
    @DisplayName("한글 조합 중인 입력")
    class Hangul {

        @ParameterizedTest(name = "{0} → 스프링")
        @ValueSource(strings = {"스", "슾", "스프", "스플", "스프리", "스프링", "ㅅ", "스ㅍ"})
        @DisplayName("\"스프링\"을 치는 동안 입력창에 나오는 모든 글자에서 스프링을 찾는다")
        void whileComposing(String input) throws Exception {
            savePost("스프링 입문");

            autocomplete(input).andExpect(jsonPath("$.data[*].text", hasItem("스프링")));
        }

        @Test
        @DisplayName("받침을 넘긴 다음 글자는 그 첫소리로 시작해야 한다 (스플 → 스프링은 되고 스프트는 안 됨)")
        void nextInitialOnly() throws Exception {
            savePost("스프링 스프트");

            autocomplete("스플").andExpect(jsonPath("$.data[*].text", contains("스프링")));
        }

        @Test
        @DisplayName("겹받침은 뒤 자음이 넘어간다 (닭 → 달걀)")
        void doubleFinal() throws Exception {
            savePost("달걀 요리");

            autocomplete("닭").andExpect(jsonPath("$.data[*].text", contains("달걀")));
        }

        @Test
        @DisplayName("한글 태그도 조합 중에 찾는다 (스플 → 스프링 부트)")
        void hangulTag() throws Exception {
            tag("스프링 부트");

            autocomplete("스플").andExpect(jsonPath("$.data[0].text").value("스프링 부트"));
        }
    }

    @Nested
    @DisplayName("경계·예외")
    class Boundary {

        @ParameterizedTest(name = "q=[{0}]")
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("비어 있거나 공백만 있으면 400이 아니라 빈 목록")
        void blank(String q) throws Exception {
            savePost("Spring 입문");

            autocomplete(q)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("q가 없어도 빈 목록")
        void missing() throws Exception {
            mockMvc.perform(get("/api/v1/search/autocomplete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("50자는 허용하고 51자는 400")
        void length() throws Exception {
            autocomplete("가".repeat(50)).andExpect(status().isOk());
            autocomplete("가".repeat(51))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("한 글자도 찾는다")
        void oneLetter() throws Exception {
            savePost("Spring 입문");

            autocomplete("s").andExpect(jsonPath("$.data[*].text", contains("spring")));
        }

        @ParameterizedTest(name = "q={0}")
        @ValueSource(strings = {"%", "_", "\\"})
        @DisplayName("LIKE 특수문자는 글자 그대로 비교해서 아무 단어나 나오지 않는다")
        void likeCharacters(String q) throws Exception {
            savePost("Spring 입문");

            autocomplete(q).andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("맞는 단어가 없으면 빈 목록")
        void noMatch() throws Exception {
            savePost("Spring 입문");

            autocomplete("kotlin").andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("[보안] 공개 범위")
    class Security {

        @Test
        @DisplayName("[보안] 숨김·삭제 글 제목의 단어는 후보가 아니다")
        void hiddenAndDeletedTitles() throws Exception {
            Post hidden = post("Kubernetes 숨김 글", false);
            hidden.hide();
            persist(hidden);
            Post deleted = post("Kubeflow 삭제 글", false);
            deleted.delete();
            persist(deleted);

            autocomplete("kube").andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("[보안] 유료 글은 공개된 제목의 단어만 후보이고, 본문 단어는 후보가 아니다")
        void paidContent() throws Exception {
            persist(Post.builder().userId(author.getId()).title("Terraform 강의")
                    .content("terragrunt 비밀 노하우").paid(true).priceChips(30).build());

            autocomplete("terra")
                    .andExpect(jsonPath("$.data[*].text", contains("terraform")));
        }

        @Test
        @DisplayName("[보안] 숨김 데모 제목의 단어는 후보가 아니다")
        void hiddenDemo() throws Exception {
            Demo hidden = demo("Websocket 숨김 데모");
            hidden.hide();
            persist(hidden);

            autocomplete("webs").andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("[한계] 지금 방식으로는 안 되는 것")
    class Limitations {

        @Test
        @DisplayName("[한계] 오타가 섞인 접두어는 찾지 못한다(sprn) → 엔터 후 3단계 교정이 처리")
        void typoInPrefix() throws Exception {
            savePost("Spring 입문");

            autocomplete("sprn").andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("[한계] 마지막 단어만 완성해서, 앞 단어와 같이 나오는 글이 없는 조합도 추천한다")
        void noCoOccurrence() throws Exception {
            savePost("Spring 입문");
            savePost("Boot 캠프 후기");

            autocomplete("spring b").andExpect(jsonPath("$.data[*].text", contains("spring boot")));
        }

        @Test
        @DisplayName("[한계] 본문에만 나오는 단어는 후보가 아니다(사전이 제목·태그뿐)")
        void contentOnlyWord() throws Exception {
            savePost("채팅 서버 회고", "netty로 소켓 서버를 만들었다");

            autocomplete("net").andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("[한계] 단어 중간부터는 찾지 못한다(boot로 springboot를 찾지 못함)")
        void middleOfWord() throws Exception {
            savePost("Springboot 정리");

            autocomplete("boot").andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    private ResultActions autocomplete(String q) throws Exception {
        return mockMvc.perform(get("/api/v1/search/autocomplete").param("q", q));
    }

    private <T> T persist(T entity) {
        return tx.execute(status -> {
            em.persist(entity);
            return entity;
        });
    }

    private void tag(String name) {
        persist(Tag.builder().name(name).build());
    }

    private void savePost(String title) {
        savePost(title, "본문");
    }

    private void savePost(String title, String content) {
        persist(Post.builder().userId(author.getId()).title(title).content(content).paid(false).priceChips(0).build());
    }

    private Post post(String title, boolean paid) {
        return Post.builder().userId(author.getId()).title(title).content("본문").paid(paid).priceChips(paid ? 30 : 0).build();
    }

    private void saveDemo(String title) {
        persist(demo(title));
    }

    private Demo demo(String title) {
        return Demo.builder().userId(author.getId()).title(title).description("설명").demoType(DemoType.STATIC).build();
    }
}
