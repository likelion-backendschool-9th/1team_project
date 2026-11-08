package com.dipsource.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.dto.search.SearchType;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.service.search.index.SearchIndexManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
        "search.fallback-to-postgres=false",
        "search.ranking.window-cache-ttl=0s",
        "search.elasticsearch.bootstrap-on-empty=false"
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
    @Autowired
    com.dipsource.service.search.suggest.SuggestIndexService suggestIndex;

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
    @ValueSource(strings = {" ", "   "})
    @DisplayName("공백만 입력하면 거절한다")
    void tooShort(String q) throws Exception {
        search(q).andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "q=[{0}]")
    @ValueSource(strings = {"a", "a ", "스", "AI", "DB", "스프", "C", "R", "#c"})
    @DisplayName("한 글자부터 검색할 수 있다")
    void allowed(String q) throws Exception {
        reindex();
        search(q).andExpect(status().isOk());
    }

    @Test
    @DisplayName("이모지 한 글자도 오류 없이 처리한다")
    void emojiIsOneCharacter() throws Exception {
        reindex();
        search("😀").andExpect(status().isOk());
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
            String body = mockMvc.perform(get("/search").param("q", "spring").param("type", "post")
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

        mockMvc.perform(get("/search").param("q", "spring"))
                .andExpect(jsonPath("$.data.posts.totalElements").value(1))
                .andExpect(jsonPath("$.data.users.totalElements").value(1))
                .andExpect(jsonPath("$.data.demos.totalElements").value(0))
                .andExpect(jsonPath("$.data.feeds.totalElements").value(0));
    }

    @Test
    @DisplayName("A~F 문서: 제목 일치가 위, 단어 반복·무관한 인기 글은 아래")
    void qualityOrdering() throws Exception {
        long a = post("Spring 핵심 개념 정리", "프레임워크의 구조를 다룬다", "ACTIVE");
        long b = post("백엔드 프레임워크 선택기", "우리 팀이 spring 을 고른 이유. spring 생태계 spring 커뮤니티 spring 문서 spring 안정성", "ACTIVE");
        long c = post("검색 잘되는 글", "spring ".repeat(100), "ACTIVE");
        long d = post("오늘의 개발 일기", "내용 없는 일기", "ACTIVE");
        tag("post_tags", "post_id", d, "spring");
        long e = post("맛집 탐방 후기 모음", "주말에 다녀온 맛집과 카페를 정리했다. 음식 사진과 웨이팅 정보가 들어 있다. 참고로 spring 시즌 메뉴가 좋았다.", "ACTIVE");
        jdbc.update("UPDATE posts SET like_count = 1000, view_count = 50000 WHERE id = ?", e);
        long f = post("Spring 완벽 가이드: Spring으로 시작하기", "Spring 의 IoC 와 DI. Spring MVC 와 Spring 설정.", "ACTIVE");
        tag("post_tags", "post_id", f, "spring");
        reindex();

        List<Integer> ids = ids(search("spring"));

        assertThat(ids.indexOf((int) f)).as("제목·본문 모두 관련 F").isLessThan(ids.indexOf((int) b));
        assertThat(ids.indexOf((int) a)).as("제목 일치 A가 본문 5회 B보다 위").isLessThan(ids.indexOf((int) b));
        assertThat(ids.indexOf((int) b)).as("정상 본문 B가 단어 반복 C보다 위").isLessThan(ids.indexOf((int) c));
        assertThat(ids.indexOf((int) a)).as("제목 일치 A가 인기만 많은 E보다 위").isLessThan(ids.indexOf((int) e));
        assertThat(ids.indexOf((int) d)).as("태그만 있는 D도 찾는다").isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("닉네임 정확 일치는 팔로워가 많은 비슷한 이름보다 먼저다")
    void exactNicknameFirst() throws Exception {
        long exact = user("springdev", "소개");
        long famous = user("springdev_official", "공식");
        for (int i = 0; i < 20; i++) {
            long follower = user("follower" + i, null);
            jdbc.update("INSERT INTO follows(follower_id, following_id, created_at) VALUES (?, ?, now())", follower, famous);
        }
        reindex();

        ResultActions result = mockMvc.perform(get("/search").param("q", "springdev").param("type", "user"));
        assertThat(ids(result).get(0)).isEqualTo((int) exact);
    }

    @Test
    @DisplayName("재정렬해도 페이지를 넘길 때 중복·누락이 없다")
    void rerankingKeepsPagesConsistent() throws Exception {
        for (int i = 0; i < 45; i++) {
            long id = post("Spring 글 " + i, "본문 " + i, "ACTIVE");
            jdbc.update("UPDATE posts SET like_count = ? WHERE id = ?", i * 7 % 40, id);
        }
        reindex();

        List<Integer> seen = new ArrayList<>();
        for (int page = 0; page < 5; page++) {
            seen.addAll(ids(mockMvc.perform(get("/search").param("q", "spring").param("type", "post")
                    .param("page", String.valueOf(page)).param("size", "10"))));
        }
        assertThat(seen).hasSize(45).doesNotHaveDuplicates();
    }

    @Nested
    @DisplayName("다국어·기술 용어·해시태그")
    class Analysis {

        @ParameterizedTest(name = "q={0} → {1}")
        @CsvSource({"C++, C++ 포인터", "c++, C++ 포인터", "C#, C# 비동기", ".NET, .NET 미니멀 API", "Node.js, Node.js 이벤트 루프", "NODE.JS, Node.js 이벤트 루프"})
        @DisplayName("특수문자 기술 용어는 다른 언어와 섞이지 않는다")
        void techTerms(String q, String expected) throws Exception {
            post("C++ 포인터", "메모리", "ACTIVE");
            post("C# 비동기", "태스크", "ACTIVE");
            post("C 언어 기초", "포인터", "ACTIVE");
            post(".NET 미니멀 API", "경량", "ACTIVE");
            post("Node.js 이벤트 루프", "libuv", "ACTIVE");
            post("트리 node 순회", "js 구현 비교", "ACTIVE");
            reindex();

            search(q).andExpect(jsonPath("$.data.content[*].title", contains(expected)));
        }

        @Test
        @DisplayName("java 검색에 JavaScript 글이 섞이지 않는다")
        void wordBoundary() throws Exception {
            post("Java 스트림", "컬렉션", "ACTIVE");
            post("JavaScript 클로저", "스코프", "ACTIVE");
            reindex();

            search("java").andExpect(jsonPath("$.data.content[*].title", contains("Java 스트림")));
        }

        @Test
        @DisplayName("조사가 붙은 한국어 문장에서 핵심 단어로 찾는다")
        void koreanMorphology() throws Exception {
            post("스프링으로 로그인 기능 만드는 방법", "세션 로그인 구현", "ACTIVE");
            post("요리 레시피", "된장찌개 끓이기", "ACTIVE");
            reindex();

            search("스프링 로그인을 만들기").andExpect(jsonPath("$.data.content[*].title", contains("스프링으로 로그인 기능 만드는 방법")));
        }

        @Test
        @DisplayName("부정 표현이 있는 문서가 그렇지 않은 문서보다 위다")
        void negation() throws Exception {
            long fixed = post("로그인이 안 되는 문제 해결", "쿠키 설정이 잘못돼서 로그인이 안 됐다", "ACTIVE");
            long other = post("로그인 되는 문제 정리", "로그인이 잘 된다", "ACTIVE");
            reindex();

            assertThat(ids(search("로그인이 안 되는 문제")).get(0)).isEqualTo((int) fixed);
            assertThat(ids(search("로그인이 안 되는 문제")).indexOf((int) other)).isNotEqualTo(0);
        }

        @Test
        @DisplayName("동의어는 찾아 주되 원문 일치가 먼저다")
        void synonymsBelowExact() throws Exception {
            long ko = post("리액트 훅 입문", "훅 사용법", "ACTIVE");
            long en = post("React 상태 관리", "리덕스", "ACTIVE");
            reindex();

            assertThat(ids(search("리액트"))).containsExactly((int) ko, (int) en);
            assertThat(ids(search("react"))).containsExactly((int) en, (int) ko);
        }

        @Test
        @DisplayName("여러 단어 동의어(스프링 부트 ↔ Spring Boot)를 찾는다")
        void multiWordSynonym() throws Exception {
            long en = post("Spring Boot 시작하기", "자동 설정", "ACTIVE");
            reindex();

            assertThat(ids(search("스프링 부트"))).containsExactly((int) en);
        }

        @Test
        @DisplayName("일본어와 중국어 제목을 해당 언어로 찾는다")
        void japaneseAndChinese() throws Exception {
            long ja = post("Springでログイン機能を作る方法", "ログイン画面", "ACTIVE");
            long zh = post("如何连接数据库", "配置数据源", "ACTIVE");
            reindex();

            assertThat(ids(search("ログイン機能"))).containsExactly((int) ja);
            assertThat(ids(search("数据库"))).containsExactly((int) zh);
        }

        @Test
        @DisplayName("#spring은 태그 정확 일치만 찾고 본문 언급은 뺀다")
        void explicitHashtag() throws Exception {
            long tagged = post("태그 글", "내용", "ACTIVE");
            tag("post_tags", "post_id", tagged, "Spring");
            post("본문 글", "spring 이야기", "ACTIVE");
            reindex();

            assertThat(ids(search("#spring"))).containsExactly((int) tagged);
        }

        @Test
        @DisplayName("여러 해시태그는 기본 and, op=or면 or다")
        void multipleHashtags() throws Exception {
            long both = post("둘 다", "내용", "ACTIVE");
            tag("post_tags", "post_id", both, "spring");
            tag("post_tags", "post_id", both, "java");
            long one = post("하나만", "내용", "ACTIVE");
            tag("post_tags", "post_id", one, "spring");
            reindex();

            assertThat(ids(search("#spring #java"))).containsExactly((int) both);
            assertThat(ids(search("#spring #java", "op", "or"))).containsExactlyInAnyOrder((int) both, (int) one);
        }

        @Test
        @DisplayName("일반 검색어 + 태그 조건을 함께 건다")
        void textAndHashtag() throws Exception {
            long match = post("Spring 글", "내용", "ACTIVE");
            tag("post_tags", "post_id", match, "java");
            post("Spring 다른 글", "내용", "ACTIVE");
            reindex();

            assertThat(ids(search("spring #java"))).containsExactly((int) match);
        }

        @Test
        @DisplayName("명시적 and는 결과가 없어도 or로 넓히지 않고, op 없는 검색만 일부 일치를 표시해서 보여 준다")
        void relaxedOnlyWithoutExplicitOperator() throws Exception {
            post("Spring 글", "내용", "ACTIVE");
            reindex();

            search("spring 없는단어", "op", "and").andExpect(jsonPath("$.data.totalElements").value(0));
            search("spring 없는단어").andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.relaxedMatch").value(true));
        }
    }

    @Nested
    @DisplayName("검색어 제안과 자동완성")
    class Suggestions {

        @BeforeEach
        void seed() throws Exception {
            post("Spring Security 입문", "내용", "ACTIVE");
            post("Spring Boot 시작하기", "내용", "ACTIVE");
            post("스프링 기초", "내용", "ACTIVE");
            post("Docker 입문", "내용", "ACTIVE");
            reindex();
        }

        @Test
        @DisplayName("자판 오입력은 원래 결과를 유지하고 제안만 한다")
        void keyboardSuggestion() throws Exception {
            search("네갸ㅜㅎ")
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.suggestedQuery").value("spring"))
                    .andExpect(jsonPath("$.data.suggestionType").value("KEYBOARD"))
                    .andExpect(jsonPath("$.data.correctedQuery").doesNotExist());
        }

        @Test
        @DisplayName("영문 자판으로 친 한글도 사전에 있으면 제안한다")
        void reverseKeyboard() throws Exception {
            search("tmvmfld").andExpect(jsonPath("$.data.suggestedQuery").value("스프링"));
        }

        @ParameterizedTest(name = "q={0}")
        @ValueSource(strings = {"안녕하세요", "오늘 날씨", "스프링", "맛집 추천"})
        @DisplayName("일반 한국어는 자판 보정 제안이 붙지 않는다")
        void noFalsePositive(String q) throws Exception {
            search(q).andExpect(jsonPath("$.data.suggestionType").doesNotExist());
        }

        @Test
        @DisplayName("사전에 없는 변환은 제안하지 않는다")
        void notInDictionary() throws Exception {
            search("ㅋㅋㅋㅋㅋ").andExpect(jsonPath("$.data.suggestedQuery").doesNotExist());
        }

        @Test
        @DisplayName("대소문자가 섞여도 자판 보정이 된다")
        void keyboardWithNormalization() throws Exception {
            search("네갸ㅜㅎ ").andExpect(jsonPath("$.data.suggestedQuery").value("spring"));
        }

        @Test
        @DisplayName("오타는 결과가 있어도 제안하고 검색어를 자동으로 바꾸지 않는다")
        void typoSuggestionKeepsResults() throws Exception {
            post("sprng 설정 오류", "제목에 오타가 있는 글", "ACTIVE");
            reindex();

            search("sprng")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("sprng 설정 오류"))
                    .andExpect(jsonPath("$.data.suggestedQuery").value("spring"))
                    .andExpect(jsonPath("$.data.suggestionType").value("TYPO"));
        }

        @Test
        @DisplayName("사전에 있는 정상 단어는 오타로 보지 않는다")
        void dictionaryWordIsNotTypo() throws Exception {
            search("docker").andExpect(jsonPath("$.data.suggestedQuery").doesNotExist());
        }

        @Test
        @DisplayName("한 단어의 결과가 없으면 그 글자로 시작하는 단어의 결과를 일부 일치로 보여 주고 제안한다")
        void prefixFallback() throws Exception {
            search("스프")
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("스프링 기초"))
                    .andExpect(jsonPath("$.data.relaxedMatch").value(true))
                    .andExpect(jsonPath("$.data.suggestedQuery").value("스프링"))
                    .andExpect(jsonPath("$.data.suggestionType").value("COMPLETION"));
            search("spr").andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.relaxedMatch").value(true));
        }

        @Test
        @DisplayName("전체 검색은 결과가 없는 대상만 넓히고 결과가 있는 대상은 그대로 둔다")
        void prefixFallbackPerSection() throws Exception {
            post("스프 끓이는 법", "요리", "ACTIVE");
            jdbc.update("""
                    INSERT INTO feeds(user_id, content, status, like_count, comment_count, repost_count, created_at, updated_at)
                    VALUES (?, '스프링 공부 시작', 'ACTIVE', 0, 0, 0, now(), now())
                    """, author);
            reindex();

            mockMvc.perform(get("/search").param("q", "스프"))
                    .andExpect(jsonPath("$.data.posts.totalElements").value(1))
                    .andExpect(jsonPath("$.data.posts.content[0].title").value("스프 끓이는 법"))
                    .andExpect(jsonPath("$.data.posts.relaxedMatch").doesNotExist())
                    .andExpect(jsonPath("$.data.feeds.totalElements").value(1))
                    .andExpect(jsonPath("$.data.feeds.relaxedMatch").value(true))
                    .andExpect(jsonPath("$.data.suggestedQuery").value("스프링"));
        }

        @Test
        @DisplayName("명시적 and는 접두어로 넓히지 않는다")
        void prefixFallbackRespectsExplicitOperator() throws Exception {
            search("스프", "op", "and").andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("접두어 검색으로도 유료 본문은 찾히지 않는다")
        void prefixFallbackDoesNotLeakPaidBody() throws Exception {
            jdbc.update("""
                    INSERT INTO posts(user_id, title, content, is_paid, price_chips, status, view_count, like_count,
                                      comment_count, created_at, updated_at)
                    VALUES (?, '다른 제목', '비밀단어 본문', true, 100, 'ACTIVE', 0, 0, 0, now(), now())
                    """, author);
            reindex();

            search("비밀").andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("글자가 하나 더 붙은 오타는 짧은 단어를 제안한다")
        void repeatedLetterTypo() throws Exception {
            post("React 입문", "내용", "ACTIVE");
            reindex();

            search("reactt").andExpect(jsonPath("$.data.suggestedQuery").value("react"));
        }

        @Test
        @DisplayName("복수형은 단수형으로 고치지 않는다")
        void pluralIsNotTypo() throws Exception {
            post("token 이야기", "내용", "ACTIVE");
            post("token 정리", "내용", "ACTIVE");
            reindex();

            search("tokens").andExpect(jsonPath("$.data.suggestedQuery").doesNotExist());
        }

        @Test
        @DisplayName("어형 변화는 오타로 제안하지 않는다")
        void inflectionIsNotTypo() throws Exception {
            post("tokens 이야기", "내용", "ACTIVE");
            post("tokens 정리", "내용", "ACTIVE");
            reindex();

            search("token").andExpect(jsonPath("$.data.suggestedQuery").doesNotExist());
        }

        @ParameterizedTest(name = "입력 {0}")
        @ValueSource(strings = {"스", "슾", "스프", "스플", "스프리", "스프링"})
        @DisplayName("한글 입력 중간 상태에서도 스프링이 나온다")
        void hangulComposing(String input) throws Exception {
            assertThat(autocomplete(input)).contains("스프링");
        }

        @Test
        @DisplayName("초성으로 찾는다")
        void initialConsonants() throws Exception {
            assertThat(autocomplete("ㅅㅍㄹ")).contains("스프링");
        }

        @Test
        @DisplayName("영어 접두어로 사전 용어와 단어를 찾고 앞 단어는 유지한다")
        void englishPrefix() throws Exception {
            assertThat(autocomplete("sp")).contains("spring");
            assertThat(autocomplete("spring b")).contains("spring boot");
        }

        @Test
        @DisplayName("자동완성은 한 글자도 받는다")
        void singleCharacter() throws Exception {
            assertThat(autocomplete("s")).isNotEmpty();
        }

        @Test
        @DisplayName("자판을 잘못 두고 입력해도 사전 용어를 후보로 낸다")
        void keyboardAutocomplete() throws Exception {
            assertThat(autocomplete("네갸")).contains("spring");
        }

        @Test
        @DisplayName("입력과 같은 후보가 맨 앞이다")
        void exactFirst() throws Exception {
            assertThat(autocomplete("spring").get(0)).isEqualToIgnoringCase("spring");
        }

        private List<String> autocomplete(String q) throws Exception {
            String body = mockMvc.perform(get("/search/autocomplete").param("q", q))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            return com.jayway.jsonpath.JsonPath.read(body, "$.data[*].text");
        }
    }

    @Nested
    @DisplayName("개인화와 숨김")
    class Personalization {

        long viewer;

        @BeforeEach
        void createViewer() {
            viewer = user("검색하는사람", null);
        }

        private ResultActions searchAs(long userId, String q, String type) throws Exception {
            UserPrincipal principal = new UserPrincipal(userId, "USER");
            return mockMvc.perform(get("/search").param("q", q).param("type", type)
                    .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()))));
        }

        private ResultActions postJson(long userId, String path, String body) throws Exception {
            UserPrincipal principal = new UserPrincipal(userId, "USER");
            return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(body)
                    .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                    .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()));
        }

        private void visits(long userId, long targetId, String query, int count, int daysAgo) {
            jdbc.update("""
                    INSERT INTO search_visits(user_id, target_type, target_id, query_norm, visit_count, last_visited_at)
                    VALUES (?, 'POST', ?, ?, ?, now() - (? * interval '1 day'))
                    """, userId, targetId, query, count, daysAgo);
        }

        @BeforeEach
        void clearPersonalData() {
            jdbc.execute("TRUNCATE search_hidden_results, search_visits RESTART IDENTITY");
        }

        @Test
        @DisplayName("숨긴 글은 그 사용자에게만 안 보이고, 해제하면 다시 보인다")
        void hideAndUnhide() throws Exception {
            long target = post("Spring 글", "본문", "ACTIVE");
            long other = user("다른사용자", null);
            reindex();

            postJson(viewer, "/search/hidden", "{\"targetType\":\"post\",\"targetId\":" + target + "}").andExpect(status().isNoContent());
            postJson(viewer, "/search/hidden", "{\"targetType\":\"post\",\"targetId\":" + target + "}").andExpect(status().isNoContent());

            searchAs(viewer, "spring", "post").andExpect(jsonPath("$.data.totalElements").value(0));
            searchAs(other, "spring", "post").andExpect(jsonPath("$.data.totalElements").value(1));
            search("spring").andExpect(jsonPath("$.data.totalElements").value(1));
            assertThat(jdbc.queryForObject("SELECT count(*) FROM search_hidden_results", Long.class)).isEqualTo(1);

            UserPrincipal principal = new UserPrincipal(viewer, "USER");
            mockMvc.perform(get("/search/hidden")
                            .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()))))
                    .andExpect(jsonPath("$.data.content[0].label").value("Spring 글"));

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/search/hidden/post/" + target)
                            .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isNoContent());
            searchAs(viewer, "spring", "post").andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("숨겨도 페이지 크기가 줄지 않고 총개수가 맞다")
        void hiddenKeepsPageSize() throws Exception {
            long first = 0;
            for (int i = 0; i < 12; i++) {
                long id = post("Spring 글 " + i, "본문", "ACTIVE");
                if (i == 0) {
                    first = id;
                }
            }
            reindex();
            jdbc.update("INSERT INTO search_hidden_results(user_id, target_type, target_id, created_at) VALUES (?, 'POST', ?, now())", viewer, first);

            searchAs(viewer, "spring", "post").andExpect(jsonPath("$.data.totalElements").value(11))
                    .andExpect(jsonPath("$.data.content", hasSize(11)));
        }

        @Test
        @DisplayName("비로그인은 숨김·방문 기록 API를 쓸 수 없다")
        void anonymousRejected() throws Exception {
            mockMvc.perform(get("/search/hidden")).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("없는 대상은 숨길 수 없다")
        void hideMissing() throws Exception {
            reindex();
            postJson(viewer, "/search/hidden", "{\"targetType\":\"post\",\"targetId\":99999}").andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("차단은 양방향으로 글과 사용자를 뺀다")
        void blocks() throws Exception {
            long blockedByMe = user("spring_차단함", null);
            long blockedMe = user("spring_차단당함", null);
            long normal = user("spring_일반", null);
            jdbc.update("INSERT INTO blocks(blocker_id, blocked_id, created_at) VALUES (?, ?, now())", viewer, blockedByMe);
            jdbc.update("INSERT INTO blocks(blocker_id, blocked_id, created_at) VALUES (?, ?, now())", blockedMe, viewer);
            jdbc.update("""
                    INSERT INTO posts(user_id, title, content, is_paid, price_chips, status, view_count, like_count, comment_count, created_at, updated_at)
                    VALUES (?, 'Spring 차단 글', '본문', false, 0, 'ACTIVE', 0, 0, 0, now(), now())
                    """, blockedByMe);
            reindex();

            searchAs(viewer, "spring", "post").andExpect(jsonPath("$.data.totalElements").value(0));
            assertThat(ids(searchAs(viewer, "spring", "user"))).containsExactly((int) normal);
            search("spring").andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("관련도가 같으면 내 팔로잉이 많이 팔로우한 사람이 먼저고, 비로그인은 그렇지 않다")
        void mutualFollows() throws Exception {
            long z = user("kafka_z", "메시징");
            long y = user("kafka_y", "메시징");
            long x = user("kafka_x", "메시징");
            long a = user("친구a", null);
            long b = user("친구b", null);
            long c = user("친구c", null);
            for (long friend : List.of(a, b, c)) {
                follow(viewer, friend);
                follow(friend, x);
            }
            follow(a, y);
            for (int i = 0; i < 5; i++) {
                follow(user("팔로워" + i, null), z);
            }
            reindex();

            assertThat(ids(searchAs(viewer, "kafka", "user"))).containsExactly((int) x, (int) y, (int) z);
            assertThat(ids(mockMvc.perform(get("/search").param("q", "kafka").param("type", "user"))).get(0)).isEqualTo((int) z);
        }

        @Test
        @DisplayName("같은 검색어로 3회 방문한 글은 올라가고, 2회면 그대로다")
        void visitBoost() throws Exception {
            long older = post("Spring Boot 입문", "본문 내용", "ACTIVE");
            long newer = post("Spring Boot 입문", "본문 내용", "ACTIVE");
            reindex();

            visits(viewer, older, "spring boot", 2, 1);
            assertThat(ids(searchAs(viewer, "spring boot", "post"))).containsExactly((int) newer, (int) older);

            jdbc.update("UPDATE search_visits SET visit_count = 3");
            assertThat(ids(searchAs(viewer, "spring boot", "post"))).containsExactly((int) older, (int) newer);
        }

        @Test
        @DisplayName("다른 검색어와 오래된 방문에는 가산이 없다")
        void visitBoostScope() throws Exception {
            long older = post("Spring Boot 입문", "React 언급", "ACTIVE");
            long newer = post("Spring Boot 입문", "React 언급", "ACTIVE");
            reindex();

            visits(viewer, older, "spring boot", 5, 1);
            assertThat(ids(searchAs(viewer, "react", "post"))).containsExactly((int) newer, (int) older);

            jdbc.update("UPDATE search_visits SET last_visited_at = now() - interval '200 day'");
            assertThat(ids(searchAs(viewer, "spring boot", "post"))).containsExactly((int) newer, (int) older);
        }

        @Test
        @DisplayName("방문 가산이 있어도 관련도가 크게 낮은 글은 올라가지 못한다")
        void visitBoostCannotOvertakeRelevance() throws Exception {
            long strong = post("Spring Boot 입문 Spring Boot 정리", "Spring Boot 설정과 Spring Boot 배포", "ACTIVE");
            long weak = post("여러 프레임워크 이야기", "긴 글 가운데 spring boot 를 잠깐 언급한다. 다른 내용이 대부분이다. 다른 내용이 대부분이다.", "ACTIVE");
            reindex();

            visits(viewer, weak, "spring boot", 10, 0);
            assertThat(ids(searchAs(viewer, "spring boot", "post")).get(0)).isEqualTo((int) strong);
        }

        @Test
        @DisplayName("짧은 시간의 재방문은 세지 않는다")
        void visitDedup() throws Exception {
            long target = post("Spring 글", "본문", "ACTIVE");
            String body = "{\"targetType\":\"post\",\"targetId\":" + target + ",\"query\":\"Spring\"}";
            postJson(viewer, "/search/visits", body).andExpect(status().isNoContent());
            postJson(viewer, "/search/visits", body).andExpect(status().isNoContent());

            assertThat(jdbc.queryForObject("SELECT visit_count FROM search_visits", Integer.class)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT query_norm FROM search_visits", String.class)).isEqualTo("spring");
        }

        private void follow(long follower, long following) {
            jdbc.update("INSERT INTO follows(follower_id, following_id, created_at) VALUES (?, ?, now())", follower, following);
        }
    }

    private static List<Integer> ids(ResultActions result) throws Exception {
        return com.jayway.jsonpath.JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.data.content[*].id");
    }

    private void tag(String table, String column, long targetId, String name) {
        long tagId = jdbc.queryForObject("""
                INSERT INTO tags(name) VALUES (?) ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name RETURNING id
                """, Long.class, name);
        jdbc.update("INSERT INTO " + table + "(" + column + ", tag_id) VALUES (?, ?)", targetId, tagId);
    }

    private ResultActions search(String q, String... extra) throws Exception {
        var request = get("/search").param("q", q).param("type", "post");
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
        suggestIndex.rebuild();
    }
}
