package com.dipsource.controller.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.service.demo.thumbnail.DemoThumbnailService;
import com.jayway.jsonpath.JsonPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

/** 2차: 임시 미리보기, 썸네일, 버전 복구, 사용자별 용량. */
class DemoManagementTest extends DemoFlowSupport {

    @Autowired DemoThumbnailService thumbnailService;

    private static byte[] jpeg(int width, int height) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "jpeg", out);
        return out.toByteArray();
    }

    private long validZipTotal() throws IOException {
        return HTML.length() + "export default 1".length() + "body{}".length();
    }

    private long usedBytes(long userId) {
        return jdbc.queryForObject("SELECT used_bytes FROM demo_usages WHERE user_id = ?", Long.class, userId);
    }

    private long reservedBytes(long userId) {
        return jdbc.queryForObject("SELECT reserved_bytes FROM demo_usages WHERE user_id = ?", Long.class, userId);
    }

    private org.springframework.test.web.servlet.ResultActions previewRaw(long userId, byte[] zip) throws Exception {
        return mockMvc.perform(multipart("/demos/previews")
                .file(new MockMultipartFile("file", "demo.zip", "application/zip", zip))
                .with(authentication(as(userId))));
    }

    private org.springframework.test.web.servlet.ResultActions restoreRaw(long demoId, long versionId, Long expected) throws Exception {
        return mockMvc.perform(post("/demos/{d}/versions/{v}/restore", demoId, versionId)
                .with(authentication(as(owner)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(expected == null ? "{}" : "{\"expectedCurrentVersionId\":" + expected + "}"));
    }

    private static <T> List<T> runConcurrently(List<Callable<T>> tasks) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> task : tasks) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return task.call();
                }));
            }
            start.countDown();
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get());
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    @Nested
    class Previews {

        @Test
        @DisplayName("임시 미리보기는 비공개 경로에 저장하고 서명 URL을 준다")
        void createsSignedPreview() throws Exception {
            String body = previewRaw(owner, validZip())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.url", startsWith("https://runner.test/p/")))
                    .andReturn().getResponse().getContentAsString();

            String url = JsonPath.read(body, "$.data.url");
            assertThat(url).endsWith("/index.html");
            assertThat(storage.countUnder("previews/")).isEqualTo(3);
            assertThat(usedBytes(owner)).isEqualTo(validZipTotal());
        }

        @Test
        @DisplayName("미리보기에도 ZIP 검증을 똑같이 적용한다")
        void validatesZip() throws Exception {
            previewRaw(owner, zip(java.util.Map.of("app.js", "1"))).andExpect(status().isBadRequest());
            assertThat(storage.countUnder("previews/")).isZero();
        }

        @Test
        @DisplayName("로그인이 필요하다")
        void requiresLogin() throws Exception {
            mockMvc.perform(multipart("/demos/previews")
                            .file(new MockMultipartFile("file", "demo.zip", "application/zip", validZip())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("저장 실패 시 파일과 예약을 정리한다")
        void compensatesFailure() throws Exception {
            storage.failPutsWhen(key -> key.startsWith("previews/") && key.endsWith("app.js"));
            previewRaw(owner, validZip()).andExpect(status().isServiceUnavailable());

            assertThat(storage.countUnder("previews/")).isZero();
            assertThat(reservedBytes(owner)).isZero();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM demo_previews", Integer.class)).isZero();
        }

        @Test
        @DisplayName("만료되면 파일을 지우고 용량을 돌려준다")
        void expiredPreviewIsCleaned() throws Exception {
            previewRaw(owner, validZip()).andExpect(status().isCreated());
            cleanupService.sweep();
            assertThat(storage.countUnder("previews/")).isEqualTo(3);

            jdbc.update("UPDATE demo_previews SET expires_at = now() - interval '1 minute'");
            cleanupService.sweep();

            assertThat(storage.countUnder("previews/")).isZero();
            assertThat(usedBytes(owner)).isZero();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM demo_previews", Integer.class)).isZero();
        }

        @Test
        @DisplayName("동시에 유지하는 미리보기 수를 제한한다")
        void limitsActivePreviews() throws Exception {
            for (int i = 0; i < 3; i++) {
                previewRaw(owner, validZip()).andExpect(status().isCreated());
            }
            previewRaw(owner, validZip()).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("버전 미리보기 URL은 작성자만 받는다")
        void versionPreviewIsOwnerOnly() throws Exception {
            long demoId = createDemo(owner);
            long versionId = upload(demoId);

            mockMvc.perform(post("/demos/{d}/versions/{v}/preview", demoId, versionId).with(authentication(as(owner))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.url", startsWith("https://runner.test/p/")));
            mockMvc.perform(post("/demos/{d}/versions/{v}/preview", demoId, versionId).with(authentication(as(other))))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Thumbnails {

        private long uploadedVersion;
        private long demoId;

        private void prepare() throws Exception {
            demoId = createDemo(owner);
            uploadedVersion = upload(demoId);
        }

        private String thumbnailStatus() {
            return jdbc.queryForObject("SELECT thumbnail_status FROM demo_versions WHERE id = ?", String.class, uploadedVersion);
        }

        @Test
        @DisplayName("촬영에 성공하면 줄여서 저장하고 썸네일로 쓴다")
        void capturesThumbnail() throws Exception {
            prepare();
            byte[] shot = jpeg(1280, 800);
            screenshots.behavior = url -> shot;

            thumbnailService.captureNext();

            assertThat(thumbnailStatus()).isEqualTo("READY");
            assertThat(screenshots.lastUrl).startsWith("https://runner.test/p/");
            assertThat(storage.countUnder("demos/" + demoId + "/thumbnails/")).isEqualTo(1);
            String thumbnail = jdbc.queryForObject("SELECT thumbnail FROM demos WHERE id = ?", String.class, demoId);
            assertThat(thumbnail).startsWith("https://runner.test/thumbnails/" + demoId + "/").endsWith(".jpg");

            String key = thumbnail.replace("https://runner.test/thumbnails/" + demoId + "/", "demos/" + demoId + "/thumbnails/");
            BufferedImage stored = ImageIO.read(new java.io.ByteArrayInputStream(storage.bytes(key)));
            assertThat(stored.getWidth()).isEqualTo(640);
        }

        @Test
        @DisplayName("실패하면 원인을 남기고 미뤘다가 다시 시도한다")
        void retriesWithBackoff() throws Exception {
            prepare();
            thumbnailService.captureNext();

            assertThat(thumbnailStatus()).isEqualTo("PENDING");
            assertThat(jdbc.queryForObject("SELECT thumbnail_attempts FROM demo_versions", Integer.class)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT thumbnail_error FROM demo_versions", String.class)).contains("촬영 실패");

            // 미룬 시각 전에는 다시 찍지 않는다
            screenshots.lastUrl = null;
            thumbnailService.captureNext();
            assertThat(screenshots.lastUrl).isNull();
        }

        @Test
        @DisplayName("재시도를 다 쓰면 FAILED로 끝나고 기본 썸네일을 유지한다")
        void stopsAfterMaxAttempts() throws Exception {
            prepare();
            for (int i = 0; i < 3; i++) {
                thumbnailService.captureNext();
                jdbc.update("UPDATE demo_versions SET thumbnail_next_at = now() - interval '1 second' WHERE thumbnail_status = 'PENDING'");
            }

            assertThat(thumbnailStatus()).isEqualTo("FAILED");
            assertThat(jdbc.queryForObject("SELECT thumbnail FROM demos WHERE id = ?", String.class, demoId)).isNull();
            screenshots.lastUrl = null;
            thumbnailService.captureNext();
            assertThat(screenshots.lastUrl).isNull();
        }

        @Test
        @DisplayName("이미지가 아닌 응답도 실패로 처리한다")
        void rejectsNonImage() throws Exception {
            prepare();
            screenshots.behavior = url -> "not image".getBytes();
            thumbnailService.captureNext();
            assertThat(thumbnailStatus()).isEqualTo("PENDING");
            assertThat(storage.countUnder("demos/" + demoId + "/thumbnails/")).isZero();
        }

        @Test
        @DisplayName("직접 올린 썸네일은 자동 생성으로 덮어쓰지 않는다")
        void manualThumbnailWins() throws Exception {
            prepare();
            String body = mockMvc.perform(multipart("/demos/{id}/thumbnail", demoId)
                            .file(new MockMultipartFile("file", "a.jpg", "image/jpeg", jpeg(300, 200)))
                            .with(authentication(as(owner))))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            String manual = JsonPath.read(body, "$.data");

            byte[] shot = jpeg(1280, 800);
            screenshots.behavior = url -> shot;
            thumbnailService.captureNext();

            assertThat(thumbnailStatus()).isEqualTo("READY");
            assertThat(jdbc.queryForObject("SELECT thumbnail FROM demos WHERE id = ?", String.class, demoId)).isEqualTo(manual);
        }

        @Test
        @DisplayName("직접 올리는 썸네일은 작성자만, 이미지만 받는다")
        void manualUploadIsValidated() throws Exception {
            prepare();
            mockMvc.perform(multipart("/demos/{id}/thumbnail", demoId)
                            .file(new MockMultipartFile("file", "a.jpg", "image/jpeg", jpeg(10, 10)))
                            .with(authentication(as(other))))
                    .andExpect(status().isNotFound());
            mockMvc.perform(multipart("/demos/{id}/thumbnail", demoId)
                            .file(new MockMultipartFile("file", "a.jpg", "image/jpeg", "not image".getBytes()))
                            .with(authentication(as(owner))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("삭제된 데모는 찍지 않는다")
        void skipsDeletedDemo() throws Exception {
            prepare();
            mockMvc.perform(delete("/demos/{id}", demoId).with(authentication(as(owner)))).andExpect(status().isNoContent());
            thumbnailService.captureNext();
            assertThat(screenshots.lastUrl).isNull();
            assertThat(thumbnailStatus()).isEqualTo("SKIPPED");
        }
    }

    @Nested
    class Restore {

        @Test
        @DisplayName("버전 목록과 현재 공개 버전을 보여 준다")
        void listsVersions() throws Exception {
            long demoId = createDemo(owner);
            long first = upload(demoId);
            long second = upload(demoId);
            publishRaw(owner, demoId, first).andExpect(status().isOk());

            mockMvc.perform(get("/demos/{id}/versions", demoId).with(authentication(as(owner))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].id").value(second))
                    .andExpect(jsonPath("$.data[0].number").value(2))
                    .andExpect(jsonPath("$.data[0].current").value(false))
                    .andExpect(jsonPath("$.data[1].current").value(true))
                    .andExpect(jsonPath("$.data[1].fileCount").value(3));
            mockMvc.perform(get("/demos/{id}/versions", demoId).with(authentication(as(other))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("이전 버전으로 복구하면 포인터가 그 버전을 가리킨다")
        void restoresPreviousVersion() throws Exception {
            long demoId = createDemo(owner);
            long first = upload(demoId);
            publishRaw(owner, demoId, first).andExpect(status().isOk());
            long second = upload(demoId);
            publishRaw(owner, demoId, second).andExpect(status().isOk());
            int putsBefore = storage.putCount();

            restoreRaw(demoId, first, second)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.currentVersionId").value(first));

            assertThat(pointer(demoId)).contains("\"versionId\":" + first);
            // 다시 올리지 않고 포인터 하나만 쓴다
            assertThat(storage.putCount()).isEqualTo(putsBefore + 1);
        }

        @Test
        @DisplayName("파일이 누락된 버전으로 복구하면 현재 공개 버전을 유지한다")
        void restoreFailureKeepsCurrent() throws Exception {
            long demoId = createDemo(owner);
            long first = upload(demoId);
            long second = upload(demoId);
            publishRaw(owner, demoId, second).andExpect(status().isOk());
            String before = pointer(demoId);
            storage.remove("demos/" + demoId + "/versions/" + first + "/files/index.html");

            restoreRaw(demoId, first, second).andExpect(status().isConflict());

            assertThat(pointer(demoId)).isEqualTo(before);
            assertThat(jdbc.queryForObject("SELECT current_version_id FROM demos WHERE id = ?", Long.class, demoId)).isEqualTo(second);
        }

        @Test
        @DisplayName("화면이 오래되어 공개 버전이 달라졌으면 409")
        void rejectsStaleRestore() throws Exception {
            long demoId = createDemo(owner);
            long first = upload(demoId);
            long second = upload(demoId);
            publishRaw(owner, demoId, second).andExpect(status().isOk());

            restoreRaw(demoId, first, first).andExpect(status().isConflict());
            assertThat(pointer(demoId)).contains("\"versionId\":" + second);
        }

        @Test
        @DisplayName("타인은 복구할 수 없다")
        void otherCannotRestore() throws Exception {
            long demoId = publishedDemo();
            long versionId = jdbc.queryForObject("SELECT id FROM demo_versions", Long.class);
            mockMvc.perform(post("/demos/{d}/versions/{v}/restore", demoId, versionId)
                            .with(authentication(as(other)))
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("동시 복구 요청은 하나만 성공하고 DB와 포인터가 일치한다")
        void concurrentRestore() throws Exception {
            long demoId = createDemo(owner);
            long first = upload(demoId);
            long second = upload(demoId);
            long third = upload(demoId);
            publishRaw(owner, demoId, third).andExpect(status().isOk());

            List<Integer> statuses = runConcurrently(List.of(
                    () -> restoreRaw(demoId, first, third).andReturn().getResponse().getStatus(),
                    () -> restoreRaw(demoId, second, third).andReturn().getResponse().getStatus()));

            assertThat(statuses).containsExactlyInAnyOrder(200, 409);
            Long current = jdbc.queryForObject("SELECT current_version_id FROM demos WHERE id = ?", Long.class, demoId);
            assertThat(pointer(demoId)).contains("\"versionId\":" + current);
        }

        @Test
        @DisplayName("공개 중인 버전은 지울 수 없고, 보관 버전을 지우면 파일과 용량이 정리된다")
        void deleteVersion() throws Exception {
            long demoId = createDemo(owner);
            long first = upload(demoId);
            long second = upload(demoId);
            publishRaw(owner, demoId, second).andExpect(status().isOk());

            mockMvc.perform(delete("/demos/{d}/versions/{v}", demoId, second).with(authentication(as(owner))))
                    .andExpect(status().isConflict());
            mockMvc.perform(delete("/demos/{d}/versions/{v}", demoId, first).with(authentication(as(other))))
                    .andExpect(status().isForbidden());
            mockMvc.perform(delete("/demos/{d}/versions/{v}", demoId, first).with(authentication(as(owner))))
                    .andExpect(status().isNoContent());

            // 파일 삭제 전이어도 목록에서는 사라지고 복구할 수 없다
            mockMvc.perform(get("/demos/{id}/versions", demoId).with(authentication(as(owner))))
                    .andExpect(jsonPath("$.data", hasSize(1)));
            restoreRaw(demoId, first, null).andExpect(status().isConflict());

            cleanupService.sweep();
            assertThat(storage.countUnder("demos/" + demoId + "/versions/" + first + "/")).isZero();
            assertThat(usedBytes(owner)).isEqualTo(validZipTotal());
        }

        @Test
        @DisplayName("데모별 보관 버전 수를 넘으면 409")
        void limitsVersions() throws Exception {
            long demoId = createDemo(owner);
            for (int i = 0; i < 4; i++) {
                upload(demoId);
            }
            uploadRaw(owner, demoId, validZip()).andExpect(status().isConflict());
            assertThat(reservedBytes(owner)).isZero();
            assertThat(usedBytes(owner)).isEqualTo(validZipTotal() * 4);
        }
    }

    @Nested
    class Quota {

        private void setUsed(long userId, long used) {
            jdbc.update("UPDATE demo_usages SET used_bytes = ? WHERE user_id = ?", used, userId);
        }

        @Test
        @DisplayName("사용량과 남은 용량을 알려 준다")
        void reportsUsage() throws Exception {
            publishedDemo();
            long used = validZipTotal();
            mockMvc.perform(get("/demos/usage").with(authentication(as(owner))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.usedBytes").value(used))
                    .andExpect(jsonPath("$.data.limitBytes").value(100000))
                    .andExpect(jsonPath("$.data.remainingBytes").value(100000 - used))
                    .andExpect(jsonPath("$.data.demoCount").value(1))
                    .andExpect(jsonPath("$.data.maxDemos").value(5));
        }

        @Test
        @DisplayName("총 용량을 넘으면 저장소에 올리지 않고 거부한다")
        void rejectsOverQuota() throws Exception {
            long demoId = createDemo(owner);
            upload(demoId);
            int puts = storage.putCount();
            setUsed(owner, 100000 - 10);

            uploadRaw(owner, demoId, validZip()).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("DEMO_QUOTA_EXCEEDED"));

            assertThat(storage.putCount()).isEqualTo(puts);
            assertThat(reservedBytes(owner)).isZero();
        }

        @Test
        @DisplayName("동시 업로드로 총 용량 제한을 넘을 수 없다")
        void concurrentUploadsCannotExceedQuota() throws Exception {
            long demoA = createDemo(owner);
            long demoB = createDemo(owner);
            long demoC = createDemo(owner);
            long demoD = createDemo(owner);
            // 빈 행을 먼저 만들고 업로드 하나만 들어갈 만큼만 남긴다
            mockMvc.perform(get("/demos/usage").with(authentication(as(owner)))).andExpect(status().isOk());
            long one = validZipTotal();
            setUsed(owner, 100000 - one - 1);
            byte[] zip = validZip();

            List<Integer> statuses = runConcurrently(List.of(
                    () -> uploadRaw(owner, demoA, zip).andReturn().getResponse().getStatus(),
                    () -> uploadRaw(owner, demoB, zip).andReturn().getResponse().getStatus(),
                    () -> uploadRaw(owner, demoC, zip).andReturn().getResponse().getStatus(),
                    () -> uploadRaw(owner, demoD, zip).andReturn().getResponse().getStatus()));

            assertThat(statuses.stream().filter(s -> s == 201)).hasSize(1);
            assertThat(statuses.stream().filter(s -> s == 409)).hasSize(3);
            assertThat(usedBytes(owner)).isLessThanOrEqualTo(100000);
            assertThat(reservedBytes(owner)).isZero();
        }

        @Test
        @DisplayName("저장 실패 시 예약을 돌려준다")
        void releasesReservationOnFailure() throws Exception {
            long demoId = createDemo(owner);
            storage.failPutsWhen(key -> key.endsWith("app.js"));
            uploadRaw(owner, demoId, validZip()).andExpect(status().isServiceUnavailable());
            assertThat(reservedBytes(owner)).isZero();
            assertThat(usedBytes(owner)).isZero();

            // 이미 돌려준 FAILED 버전은 정리할 때 다시 빼지 않는다
            storage.failPutsWhen(key -> false);
            upload(demoId);
            cleanupService.sweep();
            assertThat(usedBytes(owner)).isEqualTo(validZipTotal());
        }

        @Test
        @DisplayName("오래 멈춘 업로드는 정리되며 예약을 돌려준다")
        void stuckUploadReleasesReservation() throws Exception {
            long demoId = createDemo(owner);
            long versionId = upload(demoId);
            jdbc.update("UPDATE demo_versions SET status = 'UPLOADING', updated_at = now() - interval '2 hours' WHERE id = ?", versionId);
            jdbc.update("UPDATE demo_usages SET used_bytes = 0, reserved_bytes = ? WHERE user_id = ?", validZipTotal(), owner);

            cleanupService.sweep();

            assertThat(reservedBytes(owner)).isZero();
            assertThat(jdbc.queryForObject("SELECT status FROM demo_versions", String.class)).isEqualTo("DELETED");
        }

        @Test
        @DisplayName("데모를 지우면 파일이 정리될 때 용량이 돌아온다")
        void deleteReleasesUsage() throws Exception {
            long demoId = publishedDemo();
            assertThat(usedBytes(owner)).isEqualTo(validZipTotal());

            mockMvc.perform(delete("/demos/{id}", demoId).with(authentication(as(owner)))).andExpect(status().isNoContent());
            assertThat(jdbc.queryForObject("SELECT demo_count FROM demo_usages WHERE user_id = ?", Integer.class, owner)).isZero();

            cleanupService.sweep();
            assertThat(usedBytes(owner)).isZero();
            cleanupService.sweep();
            assertThat(usedBytes(owner)).isZero();
        }

        @Test
        @DisplayName("데모 수 한도를 동시 요청으로 넘을 수 없다")
        void concurrentCreatesCannotExceedDemoLimit() throws Exception {
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                tasks.add(() -> createRaw(owner, "동시").andReturn().getResponse().getStatus());
            }
            List<Integer> statuses = runConcurrently(tasks);

            assertThat(statuses.stream().filter(s -> s == 201)).hasSize(5);
            assertThat(statuses.stream().filter(s -> s == 409)).hasSize(3);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM demos WHERE deleted_at IS NULL", Integer.class)).isEqualTo(5);
        }
    }
}
