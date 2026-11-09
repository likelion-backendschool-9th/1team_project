package com.dipsource.controller.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.service.demo.backend.DemoBackendProbe.Outcome;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

/** 4차: 외부 백엔드 연결. 기존 정적·Mock 데모가 그대로 동작하는지도 함께 본다. */
class DemoBackendTest extends DemoFlowSupport {

    private static final String API = "https://api.example.com/v1";
    private static final String HEALTH = "https://api.example.com/health";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ResultActions connect(long userId, long demoId, String apiBaseUrl, String healthCheckUrl) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("apiBaseUrl", apiBaseUrl);
        body.put("healthCheckUrl", healthCheckUrl);
        return mockMvc.perform(put("/demos/{d}/backend", demoId).with(authentication(as(userId)))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions check(long userId, long demoId) throws Exception {
        return mockMvc.perform(post("/demos/{d}/backend/health-check", demoId).with(authentication(as(userId))));
    }

    private ResultActions detail(long userId, long demoId) throws Exception {
        return mockMvc.perform(get("/demos/{d}", demoId).with(authentication(as(userId))));
    }

    private void expireCooldown(long demoId) {
        jdbc.update("UPDATE demo_type_config SET health_claimed_at = now() - interval '1 hour' WHERE demo_id = ?", demoId);
    }

    @Test
    void 백엔드_없는_데모는_NOT_CONFIGURED이고_기존처럼_실행된다() throws Exception {
        long demoId = publishedDemo();
        detail(other, demoId).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.backend.status").value("NOT_CONFIGURED"))
                .andExpect(jsonPath("$.data.backend.apiBaseUrl").doesNotExist())
                .andExpect(jsonPath("$.data.backend.healthCheckAvailable").value(false))
                .andExpect(jsonPath("$.data.runUrl").exists())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        assertThat(pointer(demoId)).doesNotContain("apiBaseUrl");
    }

    @Test
    void 초안에_등록하면_저장소를_건드리지_않고_공개할_때_포인터에_들어간다() throws Exception {
        long demoId = createDemo(owner);
        long versionId = upload(demoId);
        connect(owner, demoId, API + "/", null).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.backend.apiBaseUrl").value(API))
                .andExpect(jsonPath("$.data.backend.status").value("UNKNOWN"));
        assertThat(storage.bytes(pointerKey(demoId))).isNull();

        publishRaw(owner, demoId, versionId).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.backend.apiBaseUrl").value(API));
        assertThat(pointer(demoId)).contains("\"apiBaseUrl\":\"" + API + "\"");
    }

    @Test
    void 공개_중인_데모의_주소를_수정하고_제거하면_포인터도_바뀐다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, null).andExpect(status().isOk());
        assertThat(pointer(demoId)).contains(API);

        connect(owner, demoId, "https://new.example.com/api", null).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.backend.apiBaseUrl").value("https://new.example.com/api"));
        assertThat(pointer(demoId)).contains("new.example.com").doesNotContain("api.example.com");

        mockMvc.perform(delete("/demos/{d}/backend", demoId).with(authentication(as(owner))))
                .andExpect(status().isNoContent());
        assertThat(pointer(demoId)).doesNotContain("apiBaseUrl");
        detail(owner, demoId)
                .andExpect(jsonPath("$.data.backend.status").value("NOT_CONFIGURED"))
                .andExpect(jsonPath("$.data.runUrl").exists());
    }

    @Test
    void 버전을_바꿔_공개해도_백엔드_주소가_유지된다() throws Exception {
        long demoId = createDemo(owner);
        long first = upload(demoId);
        publishRaw(owner, demoId, first).andExpect(status().isOk());
        connect(owner, demoId, API, null).andExpect(status().isOk());
        long second = upload(demoId);
        publishRaw(owner, demoId, second).andExpect(status().isOk());
        assertThat(pointer(demoId)).contains("\"versionId\":" + second).contains(API);
    }

    @Test
    void Mock_데모에_백엔드를_붙여도_Mock은_유지된다() throws Exception {
        long demoId = createDemo(owner);
        long versionId = upload(demoId);
        String response = mockMvc.perform(post("/demos/{d}/versions/{v}/config", demoId, versionId)
                        .with(authentication(as(owner))).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mockConfig\":\"{\\\"endpoints\\\":[{\\\"method\\\":\\\"GET\\\",\\\"path\\\":\\\"/a\\\",\\\"response\\\":[]}]}\",\"sampleData\":\"\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long withMock = ((Number) JsonPath.read(response, "$.data.id")).longValue();
        publishRaw(owner, demoId, withMock).andExpect(status().isOk());
        assertThat(pointer(demoId)).contains("mockKey").doesNotContain("apiBaseUrl");

        connect(owner, demoId, API, null).andExpect(status().isOk());
        assertThat(pointer(demoId)).contains("mockKey").contains(API);
    }

    @Test
    @DisplayName("기존 수정 API는 backend 필드를 몰라도 되고, 호출해도 연결이 지워지지 않는다")
    void legacyUpdateKeepsBackend() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, null).andExpect(status().isOk());
        mockMvc.perform(put("/demos/{d}", demoId).with(authentication(as(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"새 이름\",\"description\":\"\",\"tags\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("새 이름"))
                .andExpect(jsonPath("$.data.backend.apiBaseUrl").value(API));
    }

    @Test
    @DisplayName("기능 추가 전 데이터(API 타입, 설정 행 없음)도 그대로 읽힌다")
    void legacyRows() throws Exception {
        long demoId = publishedDemo();
        jdbc.update("UPDATE demos SET demo_type = 'API' WHERE id = ?", demoId);
        detail(other, demoId).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.backend.status").value("NOT_CONFIGURED"));
        mockMvc.perform(get("/demos").with(authentication(as(other)))).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].backend.status").value("NOT_CONFIGURED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://api.example.com", "javascript:alert(1)", "https://localhost/api", "https://127.0.0.1/api",
            "https://169.254.169.254/latest", "https://[::1]/api", "https://2130706433/", "https://user:pw@example.com/api",
            "https://api.example.com/v1?token=secret", "https://runner.workers.test/api", "not a url",
    })
    void 허용하지_않는_주소는_400이고_저장되지_않는다(String url) throws Exception {
        long demoId = publishedDemo();
        String before = pointer(demoId);
        connect(owner, demoId, url, null).andExpect(status().isBadRequest());
        detail(owner, demoId).andExpect(jsonPath("$.data.backend.status").value("NOT_CONFIGURED"));
        assertThat(pointer(demoId)).isEqualTo(before);
    }

    @Test
    void 빈_주소는_400이다() throws Exception {
        connect(owner, createDemo(owner), "  ", null).andExpect(status().isBadRequest());
    }

    @Test
    void 헬스체크_주소는_같은_출처여야_한다() throws Exception {
        connect(owner, createDemo(owner), API, "https://evil.example.org/health").andExpect(status().isBadRequest());
    }

    @Test
    void 다른_사용자는_변경할_수_없다() throws Exception {
        long demoId = publishedDemo();
        connect(other, demoId, API, null).andExpect(status().isForbidden());
        mockMvc.perform(delete("/demos/{d}/backend", demoId).with(authentication(as(other))))
                .andExpect(status().isForbidden());
        assertThat(pointer(demoId)).doesNotContain("apiBaseUrl");
    }

    @Test
    void 비로그인은_변경도_상태_확인도_할_수_없다() throws Exception {
        long demoId = publishedDemo();
        mockMvc.perform(put("/demos/{d}/backend", demoId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"apiBaseUrl\":\"" + API + "\"}")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/demos/{d}/backend/health-check", demoId)).andExpect(status().isUnauthorized());
        assertThat(backendProbe.calls).hasValue(0);
    }

    @Test
    void 숨김_데모는_변경할_수_없다() throws Exception {
        long demoId = createDemo(owner);
        jdbc.update("UPDATE demos SET status = 'HIDDEN' WHERE id = ?", demoId);
        connect(owner, demoId, API, null).andExpect(status().isForbidden());
    }

    @Test
    void 초안의_백엔드는_다른_사람이_확인할_수_없다() throws Exception {
        long demoId = createDemo(owner);
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        check(other, demoId).andExpect(status().isNotFound());
        assertThat(backendProbe.calls).hasValue(0);
    }

    @Test
    void 헬스체크_전에는_URL이_있어도_ONLINE이_아니다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.backend.status").value("UNKNOWN"))
                .andExpect(jsonPath("$.data.backend.healthCheckAvailable").value(true))
                .andExpect(jsonPath("$.data.backend.lastCheckedAt").doesNotExist());
        assertThat(backendProbe.calls).hasValue(0);
    }

    @Test
    void 헬스체크_주소가_없으면_요청하지_않고_UNKNOWN이다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, null).andExpect(status().isOk());
        check(owner, demoId).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNKNOWN"))
                .andExpect(jsonPath("$.data.healthCheckAvailable").value(false));
        assertThat(backendProbe.calls).hasValue(0);
    }

    @Test
    void 성공하면_ONLINE_실패하면_OFFLINE이다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        backendProbe.outcome = Outcome.ONLINE;
        check(other, demoId).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ONLINE"))
                .andExpect(jsonPath("$.data.lastCheckedAt").exists());
        assertThat(backendProbe.lastUrl).hasToString(HEALTH);

        expireCooldown(demoId);
        backendProbe.outcome = Outcome.OFFLINE;
        check(other, demoId).andExpect(jsonPath("$.data.status").value("OFFLINE"));
    }

    @Test
    void 허용하지_않는_IP로_해석되면_UNKNOWN이고_이전_결과를_지운다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        check(owner, demoId).andExpect(jsonPath("$.data.status").value("ONLINE"));
        expireCooldown(demoId);
        backendProbe.outcome = Outcome.UNVERIFIABLE;
        check(owner, demoId).andExpect(jsonPath("$.data.status").value("UNKNOWN"))
                .andExpect(jsonPath("$.data.lastCheckedAt").doesNotExist());
    }

    @Test
    void 최소_간격_안의_반복_요청은_외부로_나가지_않는다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        for (int i = 0; i < 5; i++) {
            check(owner, demoId).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("ONLINE"));
        }
        assertThat(backendProbe.calls).hasValue(1);
    }

    @Test
    void 동시에_요청해도_외부_요청은_한_번이다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        CountDownLatch start = new CountDownLatch(1);
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            tasks.add(() -> {
                start.await();
                return check(owner, demoId).andReturn().getResponse().getStatus();
            });
        }
        try (ExecutorService executor = Executors.newFixedThreadPool(12)) {
            List<Future<Integer>> results = tasks.stream().map(executor::submit).toList();
            start.countDown();
            for (Future<Integer> result : results) {
                assertThat(result.get()).isEqualTo(200);
            }
        }
        assertThat(backendProbe.calls).hasValue(1);
    }

    @Test
    void 주소를_지웠다_다시_등록해도_최소_간격을_건너뛰지_못한다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        check(owner, demoId).andExpect(status().isOk());
        mockMvc.perform(delete("/demos/{d}/backend", demoId).with(authentication(as(owner))))
                .andExpect(status().isNoContent());
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        check(owner, demoId).andExpect(jsonPath("$.data.status").value("UNKNOWN"));
        assertThat(backendProbe.calls).hasValue(1);
    }

    @Test
    void 헬스체크_주소를_바꾸면_이전_결과를_버린다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        check(owner, demoId).andExpect(jsonPath("$.data.status").value("ONLINE"));
        connect(owner, demoId, API, "https://api.example.com/ping").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.backend.status").value("UNKNOWN"));
    }

    @Test
    void 오래된_결과는_UNKNOWN이다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        check(owner, demoId).andExpect(jsonPath("$.data.status").value("ONLINE"));
        jdbc.update("UPDATE demo_type_config SET last_health_at = now() - interval '2 hours' WHERE demo_id = ?", demoId);
        detail(owner, demoId).andExpect(jsonPath("$.data.backend.status").value("UNKNOWN"));
    }

    @Test
    void 헬스체크_주소는_작성자에게만_보인다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        detail(owner, demoId).andExpect(jsonPath("$.data.backend.healthCheckUrl").value(HEALTH));
        detail(other, demoId)
                .andExpect(jsonPath("$.data.backend.apiBaseUrl").value(API))
                .andExpect(jsonPath("$.data.backend.healthCheckUrl").doesNotExist());
    }

    @Test
    @DisplayName("외부 서버가 죽어도 조회·실행·좋아요는 외부 호출 없이 동작한다")
    void downstreamFailureIsolated() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        backendProbe.outcome = Outcome.OFFLINE;
        backendProbe.calls.set(0);

        detail(other, demoId).andExpect(status().isOk()).andExpect(jsonPath("$.data.runUrl").exists());
        mockMvc.perform(get("/demos")).andExpect(status().isOk());
        mockMvc.perform(post("/demos/{d}/play", demoId).with(authentication(as(other)))).andExpect(status().isNoContent());
        mockMvc.perform(post("/demos/{d}/like", demoId).with(authentication(as(other)))).andExpect(status().isNoContent());
        assertThat(backendProbe.calls).hasValue(0);

        check(other, demoId).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("OFFLINE"));
        detail(other, demoId).andExpect(status().isOk()).andExpect(jsonPath("$.data.runUrl").exists());
    }

    @Test
    void 프로브가_예외를_던져도_500이_아니라_마지막_상태를_돌려준다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        backendProbe.failure = new IllegalStateException("probe failed");
        check(owner, demoId).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("UNKNOWN"));
    }

    @Test
    void 삭제된_데모는_상태를_확인할_수_없다() throws Exception {
        long demoId = publishedDemo();
        connect(owner, demoId, API, HEALTH).andExpect(status().isOk());
        mockMvc.perform(delete("/demos/{d}", demoId).with(authentication(as(owner)))).andExpect(status().isNoContent());
        check(owner, demoId).andExpect(status().isNotFound());
        assertThat(backendProbe.calls).hasValue(0);
    }

    @Test
    void 작성자_버전_미리보기에는_백엔드_권한이_들어간다() throws Exception {
        long demoId = createDemo(owner);
        long versionId = upload(demoId);
        connect(owner, demoId, API, null).andExpect(status().isOk());
        String body = mockMvc.perform(post("/demos/{d}/versions/{v}/preview", demoId, versionId).with(authentication(as(owner))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String url = JsonPath.read(body, "$.data.url");
        String scope = url.substring("https://runner.test/p/".length()).split("/")[0];
        String grant = new String(Base64.getUrlDecoder().decode(scope), StandardCharsets.UTF_8);
        assertThat(grant).contains("\"b\":\"" + API + "\"");
    }
}
