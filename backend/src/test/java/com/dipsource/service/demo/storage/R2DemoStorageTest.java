package com.dipsource.service.demo.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.dipsource.global.config.DemoProperties;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

/** R2DemoStorage를 S3 호환 테스트 서버(Adobe S3Mock)로 검증한다. 실제 Cloudflare R2가 아니므로 S3 API 사용법까지만 보장한다. */
class R2DemoStorageTest {

    private static final String BUCKET = "demo-test";
    private static final GenericContainer<?> S3_SERVER = new GenericContainer<>("adobe/s3mock:3.12.0")
            .withExposedPorts(9090)
            .waitingFor(new HttpWaitStrategy().forPath("/favicon.ico").forPort(9090));

    private static S3Client client;
    private static R2DemoStorage storage;

    @TempDir
    Path temp;

    private String root;

    @BeforeAll
    static void start() {
        S3_SERVER.start();
        client = S3Client.builder()
                .endpointOverride(URI.create("http://" + S3_SERVER.getHost() + ":" + S3_SERVER.getMappedPort(9090)))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
        client.createBucket(builder -> builder.bucket(BUCKET));
        storage = new R2DemoStorage(client, new DemoProperties(BUCKET, "http://worker", "s", null, null, null, null));
    }

    @AfterAll
    static void stop() {
        client.close();
        S3_SERVER.stop();
    }

    @BeforeEach
    void setUp() {
        root = "t-" + UUID.randomUUID() + "/";
    }

    @Test
    @DisplayName("바이트를 저장하고 읽는다")
    void putAndGetBytes() {
        storage.putBytes(root + "a.json", "{}".getBytes(StandardCharsets.UTF_8), "application/json");
        assertThat(storage.getBytes(root + "a.json")).hasValueSatisfying(bytes -> assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("{}"));
    }

    @Test
    @DisplayName("없는 객체는 빈 값")
    void missingObject() {
        assertThat(storage.getBytes(root + "missing")).isEmpty();
    }

    @Test
    @DisplayName("파일을 Content-Type과 함께 올린다")
    void putFile() throws Exception {
        Path file = Files.writeString(temp.resolve("f0"), "console.log(1)");
        storage.putFile(root + "assets/app.js", file, "text/javascript; charset=utf-8");

        assertThat(client.headObject(HeadObjectRequest.builder().bucket(BUCKET).key(root + "assets/app.js").build()).contentType())
                .startsWith("text/javascript");
    }

    @Test
    @DisplayName("한글 경로도 저장한다")
    void koreanKey() {
        storage.putBytes(root + "이미지/사진.png", new byte[] {1}, "image/png");
        assertThat(storage.getBytes(root + "이미지/사진.png")).isPresent();
    }

    @Test
    @DisplayName("접두사 아래 파일 수와 크기를 합산한다 (페이지를 넘어도)")
    void summarizesAcrossPages() {
        byte[] bytes = new byte[3];
        for (int i = 0; i < 1005; i++) {
            storage.putBytes(root + "many/" + i, bytes, "text/plain");
        }
        storage.putBytes(root + "other/x", bytes, "text/plain");

        DemoStorage.StorageSummary summary = storage.summarize(root + "many/");
        assertThat(summary.fileCount()).isEqualTo(1005);
        assertThat(summary.totalBytes()).isEqualTo(3015);
    }

    @Test
    @DisplayName("접두사 아래만 지운다")
    void deletesOnlyPrefix() {
        storage.putBytes(root + "v1/a", new byte[] {1}, "text/plain");
        storage.putBytes(root + "v1/b/c", new byte[] {1}, "text/plain");
        storage.putBytes(root + "v10/a", new byte[] {1}, "text/plain");

        storage.deletePrefix(root + "v1/");

        assertThat(storage.summarize(root + "v1/").fileCount()).isZero();
        assertThat(storage.getBytes(root + "v10/a")).isPresent();
    }

    @Test
    @DisplayName("단일 삭제와 없는 접두사 삭제")
    void deleteSingle() {
        storage.putBytes(root + "a", new byte[] {1}, "text/plain");
        storage.delete(root + "a");
        assertThat(storage.getBytes(root + "a")).isEmpty();
        storage.deletePrefix(root + "none/");
    }
}
