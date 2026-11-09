package com.dipsource.controller.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.service.demo.DemoCleanupService;
import com.dipsource.service.demo.DemoPublicationService;
import com.dipsource.service.demo.storage.DemoStorage;
import com.dipsource.service.demo.storage.InMemoryDemoStorage;
import com.jayway.jsonpath.JsonPath;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** 등록 → 업로드 → 공개 → 소비 → 삭제 흐름을 실제 PostgreSQL과 메모리 저장소로 검증한다. */
@Testcontainers
@AutoConfigureMockMvc
@Import(DemoFlowTest.StorageConfig.class)
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test",
        "demo.worker-base-url=https://runner.test",
        "demo.cleanup-interval-ms=3600000"
})
class DemoFlowTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @TestConfiguration
    static class StorageConfig {
        @Bean
        @Primary
        InMemoryDemoStorage inMemoryDemoStorage() {
            return new InMemoryDemoStorage();
        }
    }

    private static final String HTML = "<!doctype html><html><head><script type=\"module\" src=\"./assets/app.js\"></script></head><body></body></html>";

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired InMemoryDemoStorage storage;
    @Autowired DemoStorage injectedStorage;
    @Autowired DemoCleanupService cleanupService;
    @Autowired DemoPublicationService publicationService;
    @Autowired TransactionTemplate transactionTemplate;

    private long owner;
    private long other;

    @BeforeEach
    void setUp() {
        storage.reset();
        jdbc.execute("TRUNCATE demos, demo_versions, users, tags, demo_tags, demo_likes, demo_feedbacks, demo_feedback_likes, "
                + "bookmarks, blocks RESTART IDENTITY CASCADE");
        owner = user("owner");
        other = user("other");
    }

    private long user(String nickname) {
        return jdbc.queryForObject("""
                INSERT INTO users(email, nickname, bio, tech_stacks, role, status, chip_balance,
                                  blog_subscription_price, created_at, updated_at)
                VALUES (?, ?, '', '[]', 'USER', 'ACTIVE', 0, 0, now(), now()) RETURNING id
                """, Long.class, nickname + "@test.local", nickname);
    }

    private static Authentication as(long userId) {
        UserPrincipal principal = new UserPrincipal(userId, "USER");
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private static byte[] zip(Map<String, String> files) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> file : files.entrySet()) {
                out.putNextEntry(new ZipEntry(file.getKey()));
                out.write(file.getValue().getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    private static byte[] validZip() throws IOException {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("index.html", HTML);
        files.put("assets/app.js", "export default 1");
        files.put("assets/app.css", "body{}");
        return zip(files);
    }

    private ResultActions createRaw(long userId, String title) throws Exception {
        return mockMvc.perform(post("/demos").with(authentication(as(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"" + title + "\",\"description\":\"설명\",\"tags\":[\"React\",\"vite\"]}"));
    }

    private long createDemo(long userId) throws Exception {
        String body = createRaw(userId, "내 데모").andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.id")).longValue();
    }

    private ResultActions uploadRaw(long userId, long demoId, byte[] zip) throws Exception {
        return mockMvc.perform(multipart("/demos/{id}/versions", demoId)
                .file(new MockMultipartFile("file", "demo.zip", "application/zip", zip))
                .with(authentication(as(userId))));
    }

    private long upload(long demoId) throws Exception {
        String body = uploadRaw(owner, demoId, validZip()).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.id")).longValue();
    }

    private ResultActions publishRaw(long userId, long demoId, Long versionId) throws Exception {
        return mockMvc.perform(post("/demos/{id}/publish", demoId).with(authentication(as(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(versionId == null ? "{}" : "{\"versionId\":" + versionId + "}"));
    }

    private long publishedDemo() throws Exception {
        long demoId = createDemo(owner);
        long versionId = upload(demoId);
        publishRaw(owner, demoId, versionId).andExpect(status().isOk());
        return demoId;
    }

    private String pointerKey(long demoId) {
        return "demos/" + demoId + "/publication.json";
    }

    private String pointer(long demoId) {
        return new String(storage.bytes(pointerKey(demoId)), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("업로드 → 공개 → 다른 사용자 실행 흐름")
    void fullFlow() throws Exception {
        long demoId = createDemo(owner);

        // 초안은 작성자에게만 보인다
        mockMvc.perform(get("/demos/{id}", demoId).with(authentication(as(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.runUrl").doesNotExist());
        mockMvc.perform(get("/demos/{id}", demoId).with(authentication(as(other)))).andExpect(status().isNotFound());
        mockMvc.perform(get("/demos/{id}", demoId)).andExpect(status().isNotFound());

        long versionId = upload(demoId);
        String prefix = "demos/" + demoId + "/versions/" + versionId + "/files/";
        assertThat(storage.has(prefix + "index.html")).isTrue();
        assertThat(storage.has(prefix + "assets/app.js")).isTrue();
        // 업로드만으로는 공개되지 않는다
        assertThat(storage.has(pointerKey(demoId))).isFalse();

        publishRaw(owner, demoId, versionId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        assertThat(pointer(demoId))
                .contains("\"versionId\":" + versionId)
                .contains("\"entryPath\":\"index.html\"");

        mockMvc.perform(get("/demos/{id}", demoId).with(authentication(as(other))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runUrl").value(
                        "https://runner.test/demos/" + demoId + "/versions/" + versionId + "/index.html"))
                .andExpect(jsonPath("$.data.techTags", hasSize(2)))
                .andExpect(jsonPath("$.data.author.nickname").value("owner"))
                .andExpect(jsonPath("$.data.currentVersionId").value(versionId));
    }

    @Test
    @DisplayName("비로그인 쓰기는 401")
    void unauthenticatedWritesAreRejected() throws Exception {
        mockMvc.perform(post("/demos").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"x\"}"))
                .andExpect(status().isUnauthorized());
        long demoId = createDemo(owner);
        mockMvc.perform(multipart("/demos/{id}/versions", demoId)
                        .file(new MockMultipartFile("file", "demo.zip", "application/zip", validZip())))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/demos/{id}/publish", demoId)).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/demos/{id}", demoId)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("타인의 데모는 수정·삭제·업로드·공개할 수 없다")
    void otherUsersAreBlocked() throws Exception {
        long demoId = publishedDemo();
        String body = "{\"title\":\"탈취\"}";

        mockMvc.perform(put("/demos/{id}", demoId).with(authentication(as(other)))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mockMvc.perform(delete("/demos/{id}", demoId).with(authentication(as(other)))).andExpect(status().isForbidden());
        uploadRaw(other, demoId, validZip()).andExpect(status().isForbidden());
        publishRaw(other, demoId, null).andExpect(status().isForbidden());
        mockMvc.perform(post("/demos/{id}/unpublish", demoId).with(authentication(as(other)))).andExpect(status().isForbidden());

        assertThat(jdbc.queryForObject("SELECT title FROM demos WHERE id = ?", String.class, demoId)).isEqualTo("내 데모");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM demo_versions", Integer.class)).isEqualTo(1);
        assertThat(storage.has(pointerKey(demoId))).isTrue();
    }

    @Test
    @DisplayName("타인의 초안은 존재도 드러나지 않는다")
    void draftOfOthersIs404() throws Exception {
        long demoId = createDemo(owner);
        mockMvc.perform(put("/demos/{id}", demoId).with(authentication(as(other)))
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"x\"}")).andExpect(status().isNotFound());
        uploadRaw(other, demoId, validZip()).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("본인 데모 수정: 제목·태그가 바뀐다")
    void ownerCanUpdate() throws Exception {
        long demoId = publishedDemo();
        mockMvc.perform(put("/demos/{id}", demoId).with(authentication(as(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"새 이름\",\"description\":\"새 설명\",\"tags\":[\"vue\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("새 이름"))
                .andExpect(jsonPath("$.data.techTags[0]").value("vue"));
    }

    @Test
    @DisplayName("삭제하면 포인터가 바로 사라지고 파일은 정리 작업이 지운다")
    void deleteRevokesPointerAndPurges() throws Exception {
        long demoId = publishedDemo();
        mockMvc.perform(delete("/demos/{id}", demoId).with(authentication(as(owner)))).andExpect(status().isNoContent());

        assertThat(storage.has(pointerKey(demoId))).isFalse();
        mockMvc.perform(get("/demos/{id}", demoId).with(authentication(as(owner)))).andExpect(status().isNotFound());
        assertThat(storage.countUnder("demos/" + demoId + "/versions/")).isEqualTo(3);

        cleanupService.sweep();

        assertThat(storage.countUnder("demos/" + demoId + "/")).isZero();
        assertThat(jdbc.queryForObject("SELECT status FROM demo_versions", String.class)).isEqualTo("DELETED");
    }

    @Test
    @DisplayName("R2 장애로 정리가 실패하면 다음 주기에 다시 지운다")
    void cleanupRetriesAfterFailure() throws Exception {
        long demoId = publishedDemo();
        mockMvc.perform(delete("/demos/{id}", demoId).with(authentication(as(owner)))).andExpect(status().isNoContent());

        storage.failDeletesWhen(key -> key.contains("/versions/"));
        cleanupService.sweep();
        assertThat(storage.countUnder("demos/" + demoId + "/versions/")).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT status FROM demo_versions", String.class)).isEqualTo("READY");

        storage.failDeletesWhen(key -> false);
        cleanupService.sweep();
        assertThat(storage.countUnder("demos/" + demoId + "/versions/")).isZero();
    }

    @Test
    @DisplayName("포인터를 지우지 못하면 삭제 자체를 되돌린다")
    void deleteRollsBackWhenPointerRemovalFails() throws Exception {
        long demoId = publishedDemo();
        storage.failDeletesWhen(key -> key.endsWith("publication.json"));

        mockMvc.perform(delete("/demos/{id}", demoId).with(authentication(as(owner))))
                .andExpect(status().isServiceUnavailable());

        assertThat(jdbc.queryForObject("SELECT status FROM demos WHERE id = ?", String.class, demoId)).isEqualTo("ACTIVE");
        assertThat(storage.has(pointerKey(demoId))).isTrue();
    }

    @Test
    @DisplayName("관리자 삭제도 포인터를 지운다")
    void adminDeleteRevokesPointer() throws Exception {
        long demoId = publishedDemo();
        long admin = jdbc.queryForObject("""
                INSERT INTO users(email, nickname, bio, tech_stacks, role, status, chip_balance,
                                  blog_subscription_price, created_at, updated_at)
                VALUES ('admin@test.local', 'admin', '', '[]', 'ADMIN', 'ACTIVE', 0, 0, now(), now()) RETURNING id
                """, Long.class);
        UserPrincipal principal = new UserPrincipal(admin, "ADMIN");
        mockMvc.perform(delete("/admin/demos/{id}", demoId).with(authentication(
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()))))
                .andExpect(status().is2xxSuccessful());

        assertThat(storage.has(pointerKey(demoId))).isFalse();
    }

    @Test
    @DisplayName("공개를 내리면 포인터만 지우고 파일은 남긴다")
    void unpublishKeepsFiles() throws Exception {
        long demoId = publishedDemo();
        mockMvc.perform(post("/demos/{id}/unpublish", demoId).with(authentication(as(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.runUrl").doesNotExist());

        assertThat(storage.has(pointerKey(demoId))).isFalse();
        assertThat(storage.countUnder("demos/" + demoId + "/versions/")).isEqualTo(3);
        mockMvc.perform(get("/demos/{id}", demoId).with(authentication(as(other)))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("저장 중 실패하면 올린 파일을 지우고 공개 버전을 유지한다")
    void uploadFailureCompensates() throws Exception {
        long demoId = publishedDemo();
        String before = pointer(demoId);
        storage.failPutsWhen(key -> key.endsWith("assets/app.js"));

        uploadRaw(owner, demoId, validZip()).andExpect(status().isServiceUnavailable());

        Long failedId = jdbc.queryForObject("SELECT id FROM demo_versions WHERE status = 'FAILED'", Long.class);
        assertThat(storage.countUnder("demos/" + demoId + "/versions/" + failedId + "/")).isZero();
        assertThat(pointer(demoId)).isEqualTo(before);
        publishRaw(owner, demoId, failedId).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("악성 ZIP은 저장소에 아무것도 올리지 않는다")
    void maliciousZipNeverReachesStorage() throws Exception {
        long demoId = createDemo(owner);
        int putsBefore = storage.putCount();
        Map<String, String> slip = new LinkedHashMap<>();
        slip.put("index.html", HTML);
        slip.put("../../evil.js", "x");

        uploadRaw(owner, demoId, zip(slip)).andExpect(status().isBadRequest());
        uploadRaw(owner, demoId, "not zip".getBytes(StandardCharsets.UTF_8)).andExpect(status().isBadRequest());
        uploadRaw(owner, demoId, zip(Map.of("app.js", "1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message", containsString("index.html")));

        assertThat(storage.putCount()).isEqualTo(putsBefore);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM demo_versions", Integer.class)).isZero();
    }

    @Test
    @DisplayName("파일이 누락된 버전은 공개하지 않고 현재 공개 버전을 유지한다")
    void publishRejectsMissingFiles() throws Exception {
        long demoId = publishedDemo();
        String before = pointer(demoId);
        long second = upload(demoId);
        storage.remove("demos/" + demoId + "/versions/" + second + "/files/assets/app.js");

        publishRaw(owner, demoId, second).andExpect(status().isConflict());

        assertThat(pointer(demoId)).isEqualTo(before);
    }

    @Test
    @DisplayName("포인터 쓰기가 실패하면 DB도 바뀌지 않는다")
    void publishRollsBackWhenPointerWriteFails() throws Exception {
        long demoId = createDemo(owner);
        long versionId = upload(demoId);
        storage.failPutsWhen(key -> key.endsWith("publication.json"));

        publishRaw(owner, demoId, versionId).andExpect(status().isServiceUnavailable());

        assertThat(jdbc.queryForObject("SELECT status FROM demos WHERE id = ?", String.class, demoId)).isEqualTo("DRAFT");
        assertThat(jdbc.queryForObject("SELECT current_version_id FROM demos WHERE id = ?", Long.class, demoId)).isNull();
    }

    @Test
    @DisplayName("포인터를 쓴 뒤 트랜잭션이 롤백되면 이전 포인터로 복원한다")
    void pointerIsRestoredOnRollback() throws Exception {
        long demoId = publishedDemo();
        String before = pointer(demoId);
        long second = upload(demoId);

        transactionTemplate.executeWithoutResult(tx -> {
            publicationService.publish(owner, demoId, second);
            assertThat(pointer(demoId)).contains("\"versionId\":" + second);
            tx.setRollbackOnly();
        });

        assertThat(pointer(demoId)).isEqualTo(before);
    }

    @Test
    @DisplayName("좋아요는 멱등이고 본인 데모에는 할 수 없다")
    void likes() throws Exception {
        long demoId = publishedDemo();

        mockMvc.perform(post("/demos/{id}/like", demoId).with(authentication(as(other)))).andExpect(status().isNoContent());
        mockMvc.perform(post("/demos/{id}/like", demoId).with(authentication(as(other)))).andExpect(status().isNoContent());
        mockMvc.perform(get("/demos/{id}", demoId).with(authentication(as(other))))
                .andExpect(jsonPath("$.data.likeCount").value(1))
                .andExpect(jsonPath("$.data.liked").value(true));

        mockMvc.perform(delete("/demos/{id}/like", demoId).with(authentication(as(other)))).andExpect(status().isNoContent());
        mockMvc.perform(delete("/demos/{id}/like", demoId).with(authentication(as(other)))).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT like_count FROM demos WHERE id = ?", Integer.class, demoId)).isZero();

        mockMvc.perform(post("/demos/{id}/like", demoId).with(authentication(as(owner)))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("초안에는 좋아요·댓글을 달 수 없다")
    void draftHasNoEngagement() throws Exception {
        long demoId = createDemo(owner);
        mockMvc.perform(post("/demos/{id}/like", demoId).with(authentication(as(other)))).andExpect(status().isNotFound());
        mockMvc.perform(post("/demos/{id}/comments", demoId).with(authentication(as(other)))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"x\"}")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 작성·답글·수정·삭제와 커서")
    void comments() throws Exception {
        long demoId = publishedDemo();

        String created = mockMvc.perform(post("/demos/{id}/comments", demoId).with(authentication(as(other)))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"좋아요\",\"type\":\"bug\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("bug"))
                .andReturn().getResponse().getContentAsString();
        int commentId = JsonPath.read(created, "$.data.id");

        mockMvc.perform(post("/demos/{id}/comments", demoId).with(authentication(as(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"답글\",\"parentId\":" + commentId + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/demos/{id}/comments", demoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].replies", hasSize(1)))
                .andExpect(jsonPath("$.data.hasNext").value(false));
        assertThat(jdbc.queryForObject("SELECT feedback_count FROM demos WHERE id = ?", Integer.class, demoId)).isEqualTo(2);

        // 잘못된 종류, 타인의 수정·삭제
        mockMvc.perform(post("/demos/{id}/comments", demoId).with(authentication(as(other)))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"x\",\"type\":\"nope\"}")).andExpect(status().isBadRequest());
        mockMvc.perform(put("/demos/comments/{id}", commentId).with(authentication(as(owner)))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"탈취\"}")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/demos/comments/{id}", commentId).with(authentication(as(owner)))).andExpect(status().isForbidden());

        mockMvc.perform(put("/demos/comments/{id}", commentId).with(authentication(as(other)))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"고침\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.edited").value(true));

        mockMvc.perform(delete("/demos/comments/{id}", commentId).with(authentication(as(other)))).andExpect(status().isNoContent());
        mockMvc.perform(delete("/demos/comments/{id}", commentId).with(authentication(as(other)))).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT feedback_count FROM demos WHERE id = ?", Integer.class, demoId)).isEqualTo(1);

        // 답글이 남아 있으면 삭제된 댓글 자리는 내용 없이 유지된다
        mockMvc.perform(get("/demos/{id}/comments", demoId))
                .andExpect(jsonPath("$.data.content[0].content").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].replies", hasSize(1)));
    }

    @Test
    @DisplayName("차단 관계면 없는 데모처럼 보인다")
    void blockedUsersSeeNotFound() throws Exception {
        long demoId = publishedDemo();
        jdbc.update("INSERT INTO blocks(blocker_id, blocked_id, created_at) VALUES (?, ?, now())", owner, other);

        mockMvc.perform(get("/demos/{id}", demoId).with(authentication(as(other)))).andExpect(status().isNotFound());
        mockMvc.perform(post("/demos/{id}/like", demoId).with(authentication(as(other)))).andExpect(status().isNotFound());
        mockMvc.perform(get("/demos").with(authentication(as(other))))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("목록: 공개 데모만, 정렬·태그·커서")
    void listing() throws Exception {
        long first = publishedDemo();
        long second = publishedDemo();
        long third = publishedDemo();
        long draft = createDemo(owner);
        jdbc.update("UPDATE demos SET like_count = 5 WHERE id = ?", first);
        jdbc.update("UPDATE demos SET like_count = 5 WHERE id = ?", second);
        jdbc.update("UPDATE demos SET like_count = 9 WHERE id = ?", third);

        String page1 = mockMvc.perform(get("/demos").param("sort", "popular").param("size", "2"))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].id").value(third))
                .andExpect(jsonPath("$.data.content[1].id").value(second))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andReturn().getResponse().getContentAsString();
        String cursor = JsonPath.read(page1, "$.data.nextCursor");

        mockMvc.perform(get("/demos").param("sort", "popular").param("size", "2").param("cursor", cursor))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(first))
                .andExpect(jsonPath("$.data.hasNext").value(false));

        mockMvc.perform(get("/demos").param("sort", "latest"))
                .andExpect(jsonPath("$.data.content[0].id").value(third));
        mockMvc.perform(get("/demos").param("tag", "react")).andExpect(jsonPath("$.data.content", hasSize(3)));
        mockMvc.perform(get("/demos").param("tag", "없는태그")).andExpect(jsonPath("$.data.content", hasSize(0)));
        mockMvc.perform(get("/demos").param("cursor", "bad")).andExpect(status().isBadRequest());

        // 작성자 본인의 목록에만 초안이 포함된다
        mockMvc.perform(get("/demos").param("userId", String.valueOf(owner)).with(authentication(as(owner))))
                .andExpect(jsonPath("$.data.content", hasSize(4)))
                .andExpect(jsonPath("$.data.content[0].id").exists());
        List<Integer> ids = JsonPath.read(mockMvc.perform(get("/demos").param("userId", String.valueOf(owner)).with(authentication(as(other))))
                .andReturn().getResponse().getContentAsString(), "$.data.content[*].id");
        assertThat(ids).doesNotContain((int) draft).hasSize(3);
    }

    @Test
    @DisplayName("실행 수는 공개 데모에서만 오른다")
    void playCount() throws Exception {
        long demoId = publishedDemo();
        mockMvc.perform(post("/demos/{id}/play", demoId).with(authentication(as(other)))).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT play_count FROM demos WHERE id = ?", Integer.class, demoId)).isEqualTo(1);
        long draft = createDemo(owner);
        mockMvc.perform(post("/demos/{id}/play", draft).with(authentication(as(other)))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("저장소 빈은 테스트 구현으로 교체되어 있다")
    void usesTestStorage() {
        assertThat(injectedStorage).isSameAs(storage);
    }
}
