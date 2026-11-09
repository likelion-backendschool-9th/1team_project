package com.dipsource.controller.block;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.dto.dm.DmSendRequest;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostCategory;
import com.dipsource.entity.user.User;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.service.dm.DmService;
import com.jayway.jsonpath.JsonPath;
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
class BlockControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;
    @Autowired DmService dmService;

    User me;
    User other;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE blocks, follows, users RESTART IDENTITY CASCADE");
        me = persist(User.builder().email("me@test.local").nickname("나").techStacks(List.of()).build());
        other = persist(User.builder().email("other@test.local").nickname("상대").techStacks(List.of()).build());
    }

    @Test
    @DisplayName("비로그인은 차단 목록이 401")
    void listUnauthorized() throws Exception {
        mockMvc.perform(get("/blocks")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("차단하면 목록에 나오고 해제하면 빠진다")
    void blockListUnblock() throws Exception {
        mockMvc.perform(post("/blocks/{id}", other.getId()).with(authentication(auth(me))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/blocks").with(authentication(auth(me))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(other.getId().intValue()))
                .andExpect(jsonPath("$.data[0].nickname").value("상대"));

        mockMvc.perform(delete("/blocks/{id}", other.getId()).with(authentication(auth(me))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/blocks").with(authentication(auth(me))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("차단 관계면 양쪽 다 DM을 보낼 수 없고 대화방과 내역이 숨겨진다. 해제하면 다시 보인다")
    void dmHiddenWhileBlocked() throws Exception {
        dmService.send(me.getId(), new DmSendRequest(other.getId(), "안녕"));
        block(me, other);

        assertThatThrownBy(() -> dmService.send(me.getId(), new DmSendRequest(other.getId(), "다시")))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> dmService.send(other.getId(), new DmSendRequest(me.getId(), "답장")))
                .isInstanceOf(BusinessException.class);
        mockMvc.perform(get("/dm/rooms").with(authentication(auth(other))))
                .andExpect(jsonPath("$.data.length()").value(0));
        mockMvc.perform(get("/dm/{id}", me.getId()).with(authentication(auth(other))))
                .andExpect(jsonPath("$.data.content.length()").value(0));

        mockMvc.perform(delete("/blocks/{id}", other.getId()).with(authentication(auth(me))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/dm/rooms").with(authentication(auth(other))))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("차단 관계인 회원의 블로그 댓글과 답글은 숨겨지고 페이지 커서도 끊기지 않는다")
    void blogCommentsHiddenWhileBlocked() throws Exception {
        User writer = persist(User.builder().email("writer@test.local").nickname("글쓴이").techStacks(List.of()).build());
        Long postId = persistPost(writer);
        long visible = comment(writer, postId, "보이는 댓글", null);
        comment(other, postId, "숨길 답글", visible);
        comment(other, postId, "숨길 댓글", null);
        block(me, other);

        mockMvc.perform(get("/posts/{id}/comments", postId).param("size", "1").with(authentication(auth(me))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value((int) visible))
                .andExpect(jsonPath("$.data.content[0].replies.length()").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false));

        mockMvc.perform(get("/posts/{id}/comments", postId).with(authentication(auth(writer))))
                .andExpect(jsonPath("$.data.content.length()").value(2));
        mockMvc.perform(get("/posts/{id}/comments", postId))
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    @DisplayName("차단 관계면 상대의 글에 댓글을 달거나 상대 댓글에 답글·좋아요를 할 수 없다")
    void cannotInteractWithBlockedAuthorComments() throws Exception {
        User writer = persist(User.builder().email("writer@test.local").nickname("글쓴이").techStacks(List.of()).build());
        Long writerPost = persistPost(writer);
        Long otherPost = persistPost(other);
        long otherComment = comment(other, writerPost, "상대 댓글", null);
        block(me, other);

        mockMvc.perform(post("/posts/{id}/comments", otherPost).with(authentication(auth(me)))
                        .contentType("application/json").content(commentBody("글에 댓글", null)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/posts/{id}/comments", writerPost).with(authentication(auth(me)))
                        .contentType("application/json").content(commentBody("답글", otherComment)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/posts/comments/{id}/like", otherComment).with(authentication(auth(me))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("알림·칩·북마크·미니홈피·GitHub 잔디는 비로그인이면 401")
    void memberOnlyApisRejectAnonymous() throws Exception {
        for (String path : List.of("/notifications", "/notifications/unread-count", "/coins/history",
                "/users/me/bookmarks", "/mini-homes/1", "/users/1/github/repos")) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
        mockMvc.perform(get("/notifications").with(authentication(auth(me)))).andExpect(status().isOk());
    }

    private void block(User blocker, User blocked) throws Exception {
        mockMvc.perform(post("/blocks/{id}", blocked.getId()).with(authentication(auth(blocker))))
                .andExpect(status().isNoContent());
    }

    private Long persistPost(User author) {
        return persist(Post.builder().userId(author.getId()).title("글").category(PostCategory.values()[0])
                .content("본문").build()).getId();
    }

    private long comment(User author, Long postId, String content, Long parentId) throws Exception {
        String body = mockMvc.perform(post("/posts/{id}/comments", postId).with(authentication(auth(author)))
                        .contentType("application/json").content(commentBody(content, parentId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.id")).longValue();
    }

    private static String commentBody(String content, Long parentId) {
        return "{\"content\":\"%s\",\"parentId\":%s}".formatted(content, parentId);
    }

    private Authentication auth(User user) {
        UserPrincipal principal = new UserPrincipal(user.getId(), "USER");
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
