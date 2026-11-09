package com.dipsource.controller.demo;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.service.demo.DemoCleanupService;
import com.dipsource.service.demo.DemoPublicationService;
import com.dipsource.service.demo.storage.DemoStorage;
import com.dipsource.service.demo.storage.InMemoryDemoStorage;
import com.dipsource.service.demo.thumbnail.DemoScreenshotClient;
import com.jayway.jsonpath.JsonPath;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

/** 데모 통합 테스트 공통. PostgreSQL 컨테이너와 Spring 컨텍스트를 테스트 클래스끼리 재사용한다. */
@AutoConfigureMockMvc
@Import(DemoFlowSupport.StorageConfig.class)
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test",
        "demo.worker-base-url=https://runner.test",
        "demo.signing-secret=test-signing-secret",
        "demo.cleanup-interval-ms=3600000",
        "demo.thumbnail.interval-ms=3600000",
        "demo.quota.max-demos=5",
        "demo.quota.max-storage-bytes=100000",
        "demo.quota.max-versions-per-demo=4"
})
abstract class DemoFlowSupport {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @TestConfiguration
    static class StorageConfig {
        @Bean
        @Primary
        InMemoryDemoStorage inMemoryDemoStorage() {
            return new InMemoryDemoStorage();
        }

        @Bean
        FakeScreenshotClient fakeScreenshotClient() {
            return new FakeScreenshotClient();
        }
    }

    /** 격리 브라우저 대신 쓰는 테스트 대역 */
    static class FakeScreenshotClient implements DemoScreenshotClient {
        volatile Function<String, byte[]> behavior;
        volatile String lastUrl;

        @Override
        public byte[] capture(String url) {
            lastUrl = url;
            return behavior.apply(url);
        }
    }

    static final String HTML = "<!doctype html><html><head><script type=\"module\" src=\"./assets/app.js\"></script></head><body></body></html>";

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired InMemoryDemoStorage storage;
    @Autowired DemoStorage injectedStorage;
    @Autowired DemoCleanupService cleanupService;
    @Autowired FakeScreenshotClient screenshots;
    @Autowired DemoPublicationService publicationService;
    @Autowired TransactionTemplate transactionTemplate;

    long owner;
    long other;

    @BeforeEach
    void setUp() {
        storage.reset();
        screenshots.behavior = url -> {
            throw new IllegalStateException("촬영 실패");
        };
        jdbc.execute("TRUNCATE demos, demo_versions, demo_usages, demo_previews, users, tags, demo_tags, demo_likes, "
                + "demo_feedbacks, demo_feedback_likes, bookmarks, blocks RESTART IDENTITY CASCADE");
        owner = user("owner");
        other = user("other");
    }

    long user(String nickname) {
        return jdbc.queryForObject("""
                INSERT INTO users(email, nickname, bio, tech_stacks, role, status, chip_balance,
                                  blog_subscription_price, created_at, updated_at)
                VALUES (?, ?, '', '[]', 'USER', 'ACTIVE', 0, 0, now(), now()) RETURNING id
                """, Long.class, nickname + "@test.local", nickname);
    }

    static Authentication as(long userId) {
        UserPrincipal principal = new UserPrincipal(userId, "USER");
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    static byte[] zip(Map<String, String> files) throws IOException {
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

    static byte[] validZip() throws IOException {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("index.html", HTML);
        files.put("assets/app.js", "export default 1");
        files.put("assets/app.css", "body{}");
        return zip(files);
    }

    ResultActions createRaw(long userId, String title) throws Exception {
        return mockMvc.perform(post("/demos").with(authentication(as(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"" + title + "\",\"description\":\"설명\",\"tags\":[\"React\",\"vite\"]}"));
    }

    long createDemo(long userId) throws Exception {
        String body = createRaw(userId, "내 데모").andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.id")).longValue();
    }

    ResultActions uploadRaw(long userId, long demoId, byte[] zip) throws Exception {
        return mockMvc.perform(multipart("/demos/{id}/versions", demoId)
                .file(new MockMultipartFile("file", "demo.zip", "application/zip", zip))
                .with(authentication(as(userId))));
    }

    long upload(long demoId) throws Exception {
        String body = uploadRaw(owner, demoId, validZip()).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.id")).longValue();
    }

    ResultActions publishRaw(long userId, long demoId, Long versionId) throws Exception {
        return mockMvc.perform(post("/demos/{id}/publish", demoId).with(authentication(as(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(versionId == null ? "{}" : "{\"versionId\":" + versionId + "}"));
    }

    long publishedDemo() throws Exception {
        long demoId = createDemo(owner);
        long versionId = upload(demoId);
        publishRaw(owner, demoId, versionId).andExpect(status().isOk());
        return demoId;
    }

    String pointerKey(long demoId) {
        return "demos/" + demoId + "/publication.json";
    }

    String pointer(long demoId) {
        return new String(storage.bytes(pointerKey(demoId)), StandardCharsets.UTF_8);
    }

}
