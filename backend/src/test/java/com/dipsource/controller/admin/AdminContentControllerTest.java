package com.dipsource.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoType;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedComment;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostCategory;
import com.dipsource.entity.post.PostComment;
import com.dipsource.entity.post.PostStatus;
import com.dipsource.entity.report.Report;
import com.dipsource.entity.user.User;
import com.dipsource.global.auth.UserPrincipal;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test"
})
class AdminContentControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;

    User admin;
    User author;
    User reporter1;
    User reporter2;
    Post percentPost;
    Demo percentDemo;
    Long commentFeedId;

    @BeforeEach
    void setUp() {
        jdbc.execute("""
                TRUNCATE reports, admin_action_logs, feed_comments, feeds, post_comments, posts, demos, users
                RESTART IDENTITY CASCADE
                """);
        admin = persist(User.builder().email("admin@test.local").nickname("관리자").techStacks(List.of()).build());
        author = persist(User.builder().email("author@test.local").nickname("검색러").techStacks(List.of()).build());
        reporter1 = persist(User.builder().email("r1@test.local").nickname("신고1").techStacks(List.of()).build());
        reporter2 = persist(User.builder().email("r2@test.local").nickname("신고2").techStacks(List.of()).build());

        persist(Post.builder().userId(author.getId()).title("일반글").category(PostCategory.BACKEND).content("본문").build());
        percentPost = persist(Post.builder()
                .userId(author.getId())
                .title("100% 완료")
                .category(PostCategory.BACKEND)
                .content("본문")
                .build());
        persist(Report.builder().reporterId(reporter1.getId()).targetType("POST").targetId(percentPost.getId()).reason("스팸").build());
        persist(Report.builder().reporterId(reporter2.getId()).targetType("POST").targetId(percentPost.getId()).reason("광고").build());
        persist(PostComment.builder().post(percentPost).userId(author.getId()).content("일반 블로그 댓글").build());
        persist(PostComment.builder().post(percentPost).userId(author.getId()).content("100% 블로그 댓글").build());

        Feed feed = persist(Feed.builder().userId(author.getId()).content("피드").build());
        commentFeedId = feed.getId();
        persist(FeedComment.builder().feed(feed).userId(author.getId()).content("일반댓글").build());
        FeedComment percentComment = persist(FeedComment.builder().feed(feed).userId(author.getId()).content("100% 완료").build());
        persist(Report.builder().reporterId(reporter1.getId()).targetType("FEED_COMMENT").targetId(percentComment.getId()).reason("스팸").build());

        persist(Demo.builder().userId(author.getId()).title("일반데모").description("설명").demoType(DemoType.STATIC).build());
        percentDemo = persist(Demo.builder()
                .userId(author.getId())
                .title("100% 완료")
                .description("설명")
                .demoType(DemoType.STATIC)
                .build());
        persist(Report.builder().reporterId(reporter1.getId()).targetType("DEMO").targetId(percentDemo.getId()).reason("스팸").build());
        persist(Report.builder().reporterId(reporter2.getId()).targetType("DEMO").targetId(percentDemo.getId()).reason("광고").build());
    }

    @Test
    @DisplayName("일반 회원은 관리자 글 목록이 403")
    void userForbidden() throws Exception {
        mockMvc.perform(get("/admin/posts").with(authentication(auth(author, "USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("관리자 글 목록은 닉네임·신고 수·% 이스케이프")
    void adminPosts() throws Exception {
        mockMvc.perform(get("/admin/posts")
                        .param("q", "%")
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("100% 완료"))
                .andExpect(jsonPath("$.data.content[0].author").value("검색러"))
                .andExpect(jsonPath("$.data.content[0].reported").value(2))
                .andExpect(jsonPath("$.data.content[0].metric").value("조회 0"))
                .andExpect(jsonPath("$.data.content[0].status").value("visible"));
    }

    @Test
    @DisplayName("관리자 댓글 목록은 닉네임·신고 수·% 이스케이프")
    void adminComments() throws Exception {
        mockMvc.perform(get("/admin/comments")
                        .param("q", "%")
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("100% 완료"))
                .andExpect(jsonPath("$.data.content[0].author").value("검색러"))
                .andExpect(jsonPath("$.data.content[0].reported").value(1))
                .andExpect(jsonPath("$.data.content[0].metric").value("피드 #" + commentFeedId))
                .andExpect(jsonPath("$.data.content[0].status").value("visible"));
    }

    @Test
    @DisplayName("관리자 블로그 댓글 목록은 피드 댓글과 따로, 닉네임·글 번호·% 이스케이프")
    void adminPostComments() throws Exception {
        mockMvc.perform(get("/admin/post-comments")
                        .param("q", "%")
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("100% 블로그 댓글"))
                .andExpect(jsonPath("$.data.content[0].author").value("검색러"))
                .andExpect(jsonPath("$.data.content[0].reported").value(0))
                .andExpect(jsonPath("$.data.content[0].metric").value("글 #" + percentPost.getId()))
                .andExpect(jsonPath("$.data.content[0].status").value("visible"));
    }

    @Test
    @DisplayName("일반 회원은 관리자 블로그 댓글 목록이 403")
    void userForbiddenPostComments() throws Exception {
        mockMvc.perform(get("/admin/post-comments").with(authentication(auth(author, "USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("관리자 데모 목록은 닉네임·신고 수·% 이스케이프")
    void adminDemos() throws Exception {
        mockMvc.perform(get("/admin/demos")
                        .param("q", "%")
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("100% 완료"))
                .andExpect(jsonPath("$.data.content[0].author").value("검색러"))
                .andExpect(jsonPath("$.data.content[0].reported").value(2))
                .andExpect(jsonPath("$.data.content[0].metric").value("체험 0"))
                .andExpect(jsonPath("$.data.content[0].status").value("visible"));
    }

    @Test
    @DisplayName("관리자 글 삭제 후 상태가 DELETED가 되고 작업 이력이 남는다")
    void adminDeletesPost() throws Exception {
        mockMvc.perform(delete("/admin/posts/{id}", percentPost.getId())
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isNoContent());

        em.clear();
        assertThat(em.find(Post.class, percentPost.getId()).getStatus()).isEqualTo(PostStatus.DELETED);
        Integer logs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM admin_action_logs WHERE target_type = 'POST' AND target_id = ?",
                Integer.class,
                percentPost.getId());
        assertThat(logs).isEqualTo(1);
    }

    @Test
    @DisplayName("관리자 댓글 삭제 후 소프트 삭제되고 피드 댓글 수가 줄어든다")
    void adminDeletesComment() throws Exception {
        Feed feed = persist(Feed.builder().userId(author.getId()).content("삭제 테스트 피드").build());
        FeedComment comment = persist(FeedComment.builder()
                .feed(feed)
                .userId(author.getId())
                .content("지울 댓글")
                .build());
        jdbc.update("UPDATE feeds SET comment_count = 1 WHERE id = ?", feed.getId());

        mockMvc.perform(delete("/admin/comments/{id}", comment.getId())
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isNoContent());

        em.clear();
        assertThat(em.find(FeedComment.class, comment.getId()).getDeletedAt()).isNotNull();
        assertThat(em.find(Feed.class, feed.getId()).getCommentCount()).isEqualTo(0);
        Integer logs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM admin_action_logs WHERE target_type = 'FEED_COMMENT' AND target_id = ?",
                Integer.class,
                comment.getId());
        assertThat(logs).isEqualTo(1);
    }

    @Test
    @DisplayName("관리자 블로그 댓글 삭제 후 소프트 삭제되고 글 댓글 수가 줄며, 다시 지우면 404")
    void adminDeletesPostComment() throws Exception {
        Post post = persist(Post.builder()
                .userId(author.getId())
                .title("댓글 삭제 테스트")
                .category(PostCategory.BACKEND)
                .content("본문")
                .build());
        PostComment comment = persist(PostComment.builder()
                .post(post)
                .userId(author.getId())
                .content("지울 블로그 댓글")
                .build());
        jdbc.update("UPDATE posts SET comment_count = 1 WHERE id = ?", post.getId());

        mockMvc.perform(delete("/admin/post-comments/{id}", comment.getId())
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isNoContent());

        em.clear();
        assertThat(em.find(PostComment.class, comment.getId()).getDeletedAt()).isNotNull();
        assertThat(em.find(Post.class, post.getId()).getCommentCount()).isEqualTo(0);
        Integer logs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM admin_action_logs WHERE target_type = 'POST_COMMENT' AND target_id = ?",
                Integer.class,
                comment.getId());
        assertThat(logs).isEqualTo(1);

        mockMvc.perform(delete("/admin/post-comments/{id}", comment.getId())
                        .with(authentication(auth(admin, "ADMIN"))))
                .andExpect(status().isNotFound());
    }

    private Authentication auth(User user, String role) {
        UserPrincipal principal = new UserPrincipal(user.getId(), role);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private <T> T persist(T entity) {
        return tx.execute(status -> {
            em.persist(entity);
            em.flush();
            return entity;
        });
    }
}
