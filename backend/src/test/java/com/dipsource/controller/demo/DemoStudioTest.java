package com.dipsource.controller.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.dto.demo.DemoTemplateDetail;
import com.dipsource.service.demo.DemoTemplateService;
import com.dipsource.service.demo.mock.DemoMockCompiler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

/** 3차: Mock API, 샘플 데이터, 코드 편집 저장, 템플릿. */
class DemoStudioTest extends DemoFlowSupport {

    private static final String MOCK = "{\"endpoints\":[{\"method\":\"GET\",\"path\":\"/products\",\"dataKey\":\"products\"}]}";
    private static final String SAMPLE = "{\"products\":[{\"id\":1,\"name\":\"Keyboard\"}]}";

    @Autowired ObjectMapper objectMapper;
    @Autowired DemoTemplateService templateService;
    @Autowired DemoMockCompiler mockCompiler;

    private ResultActions saveEditor(long userId, long demoId, Map<String, String> body) throws Exception {
        return mockMvc.perform(post("/demos/{id}/versions/editor", demoId).with(authentication(as(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private static Map<String, String> code(String html) {
        Map<String, String> body = new HashMap<>();
        body.put("html", html);
        body.put("css", "body{color:red}");
        body.put("js", "console.log(1)");
        return body;
    }

    private long saveEditorOk(long demoId, Map<String, String> body) throws Exception {
        String response = saveEditor(owner, demoId, body).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.data.id")).longValue();
    }

    private ResultActions saveConfig(long userId, long demoId, long versionId, String mock, String sample) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("mockConfig", mock);
        body.put("sampleData", sample);
        return mockMvc.perform(post("/demos/{d}/versions/{v}/config", demoId, versionId).with(authentication(as(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private long saveConfigOk(long demoId, long versionId, String mock, String sample) throws Exception {
        String response = saveConfig(owner, demoId, versionId, mock, sample).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.data.id")).longValue();
    }

    private String text(String key) {
        return new String(storage.bytes(key), StandardCharsets.UTF_8);
    }

    private long used() {
        return jdbc.queryForObject("SELECT used_bytes FROM demo_usages WHERE user_id = ?", Long.class, owner);
    }

    @Test
    @DisplayName("코드 편집 결과가 정적 파일 버전으로 저장되고 공개까지 된다")
    void editorSavesVersion() throws Exception {
        long demoId = createDemo(owner);
        long versionId = saveEditorOk(demoId, code("<h1>안녕</h1>"));

        String prefix = "demos/" + demoId + "/versions/" + versionId + "/files/";
        assertThat(text(prefix + "index.html"))
                .contains("<h1>안녕</h1>")
                .contains("<link rel=\"stylesheet\" href=\"./style.css\">")
                .contains("<script src=\"./script.js\"></script>")
                .startsWith("<!doctype html>");
        assertThat(text(prefix + "style.css")).isEqualTo("body{color:red}");
        assertThat(text(prefix + "script.js")).isEqualTo("console.log(1)");

        publishRaw(owner, demoId, versionId).andExpect(status().isOk());
        assertThat(pointer(demoId)).contains("\"versionId\":" + versionId).doesNotContain("mockKey");
    }

    @Test
    @DisplayName("완성된 HTML 문서는 감싸지 않고 CSS·JS만 연결한다")
    void keepsFullDocument() throws Exception {
        long demoId = createDemo(owner);
        long versionId = saveEditorOk(demoId, code("<!doctype html><html><head><title>t</title></head><body><p>x</p></body></html>"));
        String html = text("demos/" + demoId + "/versions/" + versionId + "/files/index.html");
        assertThat(html).containsOnlyOnce("<html");
        assertThat(html.indexOf("style.css")).isLessThan(html.indexOf("</head>"));
        assertThat(html.indexOf("script.js")).isLessThan(html.indexOf("</body>"));
    }

    @Test
    @DisplayName("편집기 저장에도 절대 경로 검사를 적용한다")
    void editorRejectsRootAbsolute() throws Exception {
        long demoId = createDemo(owner);
        saveEditor(owner, demoId, code("<script src=\"/evil.js\"></script>")).andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM demo_versions", Integer.class)).isZero();
    }

    @Test
    @DisplayName("코드 크기를 제한한다")
    void editorLimitsSize() throws Exception {
        long demoId = createDemo(owner);
        saveEditor(owner, demoId, code("x".repeat(262145))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("편집기 저장에도 사용자 용량을 적용한다")
    void editorChargesQuota() throws Exception {
        long demoId = createDemo(owner);
        saveEditorOk(demoId, code("<p>a</p>"));
        assertThat(used()).isPositive();

        jdbc.update("UPDATE demo_usages SET used_bytes = 99990 WHERE user_id = ?", owner);
        saveEditor(owner, demoId, code("<p>a</p>")).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("타인은 편집 저장·설정 조회·설정 저장을 할 수 없다")
    void otherUsersAreBlocked() throws Exception {
        long demoId = publishedDemo();
        long versionId = jdbc.queryForObject("SELECT id FROM demo_versions", Long.class);

        saveEditor(other, demoId, code("<p>x</p>")).andExpect(status().isForbidden());
        saveConfig(other, demoId, versionId, MOCK, SAMPLE).andExpect(status().isForbidden());
        mockMvc.perform(get("/demos/{d}/versions/{v}/config", demoId, versionId).with(authentication(as(other))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/demos/{d}/versions/{v}/config", demoId, versionId)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Mock 설정은 mock.json으로 컴파일되어 포인터에 연결된다")
    void editorWithMock() throws Exception {
        long demoId = createDemo(owner);
        Map<String, String> body = code("<p>x</p>");
        body.put("mockConfig", MOCK);
        body.put("sampleData", SAMPLE);
        long versionId = saveEditorOk(demoId, body);

        String key = "demos/" + demoId + "/versions/" + versionId + "/mock.json";
        assertThat(text(key)).contains("\"path\":\"/products\"").contains("Keyboard").doesNotContain("dataKey");

        publishRaw(owner, demoId, versionId).andExpect(status().isOk());
        assertThat(pointer(demoId)).contains("\"mockKey\":\"" + key + "\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {"{broken", "{\"endpoints\":1}", "[]"})
    @DisplayName("잘못된 Mock 설정은 저장되지 않는다")
    void rejectsInvalidMock(String config) throws Exception {
        long demoId = createDemo(owner);
        Map<String, String> body = code("<p>x</p>");
        body.put("mockConfig", config);
        saveEditor(owner, demoId, body).andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM demo_versions", Integer.class)).isZero();
    }

    @Test
    @DisplayName("JSON 문법 오류를 위치와 함께 돌려준다")
    void reportsJsonError() throws Exception {
        long demoId = publishedDemo();
        long versionId = jdbc.queryForObject("SELECT id FROM demo_versions", Long.class);
        saveConfig(owner, demoId, versionId, null, "{\n,}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message", containsString("샘플 데이터 JSON 문법 오류")));
    }

    @Test
    @DisplayName("설정 변경은 새 버전을 만들고 파일을 다시 올리지 않는다")
    void configCreatesVersionSharingFiles() throws Exception {
        long demoId = createDemo(owner);
        long source = upload(demoId);
        publishRaw(owner, demoId, source).andExpect(status().isOk());
        String before = pointer(demoId);
        long usedBefore = used();
        int putsBefore = storage.putCount();

        long derived = saveConfigOk(demoId, source, MOCK, SAMPLE);

        assertThat(derived).isNotEqualTo(source);
        assertThat(storage.putCount()).isEqualTo(putsBefore + 1);
        // 저장만으로는 공개 버전이 바뀌지 않는다
        assertThat(pointer(demoId)).isEqualTo(before);
        assertThat(used() - usedBefore).isEqualTo(storage.bytes("demos/" + demoId + "/versions/" + derived + "/mock.json").length);

        publishRaw(owner, demoId, derived).andExpect(status().isOk());
        assertThat(pointer(demoId))
                .contains("\"storagePrefix\":\"demos/" + demoId + "/versions/" + source + "\"")
                .contains("mockKey");
    }

    @Test
    @DisplayName("버전을 복구하면 그 버전의 Mock 설정으로 돌아간다")
    void restoreBringsBackMock() throws Exception {
        long demoId = createDemo(owner);
        long first = upload(demoId);
        long withMock = saveConfigOk(demoId, first, MOCK, SAMPLE);
        String otherMock = "{\"endpoints\":[{\"method\":\"GET\",\"path\":\"/users\",\"response\":[]}]}";
        long withOther = saveConfigOk(demoId, withMock, otherMock, null);
        publishRaw(owner, demoId, withOther).andExpect(status().isOk());
        assertThat(pointer(demoId)).contains("/versions/" + withOther + "/mock.json");

        mockMvc.perform(post("/demos/{d}/versions/{v}/restore", demoId, withMock).with(authentication(as(owner)))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());

        assertThat(pointer(demoId)).contains("/versions/" + withMock + "/mock.json");
        mockMvc.perform(get("/demos/{d}/versions/{v}/config", demoId, withMock).with(authentication(as(owner))))
                .andExpect(jsonPath("$.data.mockConfig").value(MOCK))
                .andExpect(jsonPath("$.data.sampleData").value(SAMPLE));

        // Mock 없는 원본으로 되돌리면 Mock도 사라진다
        mockMvc.perform(post("/demos/{d}/versions/{v}/restore", demoId, first).with(authentication(as(owner)))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        assertThat(pointer(demoId)).doesNotContain("mockKey");
    }

    @Test
    @DisplayName("편집기 코드는 설정 변경 버전에도 이어진다")
    void editorSourceCarriesOver() throws Exception {
        long demoId = createDemo(owner);
        long versionId = saveEditorOk(demoId, code("<p>원본</p>"));
        long derived = saveConfigOk(demoId, versionId, MOCK, SAMPLE);

        mockMvc.perform(get("/demos/{d}/versions/{v}/config", demoId, derived).with(authentication(as(owner))))
                .andExpect(jsonPath("$.data.editor.html").value("<p>원본</p>"))
                .andExpect(jsonPath("$.data.editor.js").value("console.log(1)"));
    }

    @Test
    @DisplayName("ZIP으로 올린 버전에는 편집기 코드가 없다")
    void zipHasNoEditorSource() throws Exception {
        long demoId = createDemo(owner);
        long versionId = upload(demoId);
        mockMvc.perform(get("/demos/{d}/versions/{v}/config", demoId, versionId).with(authentication(as(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.editor").doesNotExist());
    }

    @Test
    @DisplayName("Mock 저장이 실패하면 예약을 돌려주고 공개 버전을 유지한다")
    void configFailureCompensates() throws Exception {
        long demoId = createDemo(owner);
        long source = upload(demoId);
        publishRaw(owner, demoId, source).andExpect(status().isOk());
        String before = pointer(demoId);
        long usedBefore = used();
        storage.failPutsWhen(key -> key.endsWith("mock.json"));

        saveConfig(owner, demoId, source, MOCK, SAMPLE).andExpect(status().isServiceUnavailable());

        assertThat(pointer(demoId)).isEqualTo(before);
        assertThat(used()).isEqualTo(usedBefore);
        assertThat(jdbc.queryForObject("SELECT reserved_bytes FROM demo_usages WHERE user_id = ?", Long.class, owner)).isZero();

        // 실패한 버전을 정리해도 원본 파일은 남는다
        storage.failPutsWhen(key -> false);
        cleanupService.sweep();
        assertThat(storage.countUnder("demos/" + demoId + "/versions/" + source + "/files/")).isEqualTo(3);
        assertThat(used()).isEqualTo(usedBefore);
    }

    @Test
    @DisplayName("파일을 함께 쓰는 버전이 남아 있으면 원본을 지워도 파일을 유지하고 용량을 넘긴다")
    void deletingOwnerKeepsSharedFiles() throws Exception {
        long demoId = createDemo(owner);
        long source = upload(demoId);
        long derived = saveConfigOk(demoId, source, MOCK, SAMPLE);
        publishRaw(owner, demoId, derived).andExpect(status().isOk());
        long usedBefore = used();

        mockMvc.perform(delete("/demos/{d}/versions/{v}", demoId, source).with(authentication(as(owner))))
                .andExpect(status().isNoContent());
        cleanupService.sweep();

        assertThat(storage.countUnder("demos/" + demoId + "/versions/" + source + "/files/")).isEqualTo(3);
        assertThat(used()).isEqualTo(usedBefore);
        assertThat(jdbc.queryForObject("SELECT owns_files FROM demo_versions WHERE id = ?", Boolean.class, derived)).isTrue();

        // 파일을 여전히 온전히 공개할 수 있다
        publishRaw(owner, demoId, derived).andExpect(status().isOk());
    }

    @Test
    @DisplayName("데모를 지우면 공유 파일과 용량까지 모두 정리된다")
    void deletingDemoCleansSharedVersions() throws Exception {
        long demoId = createDemo(owner);
        long source = upload(demoId);
        saveConfigOk(demoId, source, MOCK, SAMPLE);

        mockMvc.perform(delete("/demos/{id}", demoId).with(authentication(as(owner)))).andExpect(status().isNoContent());
        cleanupService.sweep();

        assertThat(storage.countUnder("demos/" + demoId + "/")).isZero();
        assertThat(used()).isZero();
    }

    @Test
    @DisplayName("설정 변경도 버전 수 제한을 지킨다")
    void configRespectsVersionLimit() throws Exception {
        long demoId = createDemo(owner);
        long source = upload(demoId);
        for (int i = 0; i < 3; i++) {
            saveConfigOk(demoId, source, MOCK, SAMPLE);
        }
        saveConfig(owner, demoId, source, MOCK, SAMPLE).andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT reserved_bytes FROM demo_usages WHERE user_id = ?", Long.class, owner)).isZero();
    }

    @Test
    @DisplayName("템플릿 3종을 비로그인도 볼 수 있다")
    void listsTemplates() throws Exception {
        mockMvc.perform(get("/demo-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].key").value("calculator"))
                .andExpect(jsonPath("$.data[1].key").value("drag-drop"))
                .andExpect(jsonPath("$.data[2].key").value("mini-game"))
                .andExpect(jsonPath("$.data[0].usage").isNotEmpty());
        mockMvc.perform(get("/demo-templates/calculator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.html").isNotEmpty())
                .andExpect(jsonPath("$.data.js").isNotEmpty())
                .andExpect(jsonPath("$.data.sampleData").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"nope", "calculator.json"})
    @DisplayName("없는 템플릿은 404")
    void unknownTemplate(String key) throws Exception {
        mockMvc.perform(get("/demo-templates/" + key)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("템플릿 경로 조작은 요청 단계에서 막힌다")
    void templatePathTraversal() throws Exception {
        mockMvc.perform(get("/demo-templates/..%2f..%2fapplication.yml")).andExpect(status().is4xxClientError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"calculator", "drag-drop", "mini-game"})
    @DisplayName("템플릿을 그대로 저장해 공개할 수 있다")
    void templateCanBePublished(String key) throws Exception {
        DemoTemplateDetail template = templateService.get(key);
        assertThat(mockCompiler.compile(template.mockConfig(), template.sampleData()).json()).isNotNull();

        long demoId = createDemo(owner);
        Map<String, String> body = code(template.html());
        body.put("css", template.css());
        body.put("js", template.js() + "\n// 일부 수정");
        body.put("mockConfig", template.mockConfig());
        body.put("sampleData", template.sampleData());
        long versionId = saveEditorOk(demoId, body);

        publishRaw(owner, demoId, versionId).andExpect(status().isOk());
        assertThat(text("demos/" + demoId + "/versions/" + versionId + "/files/script.js")).endsWith("// 일부 수정");
        assertThat(pointer(demoId)).contains("mockKey");
    }
}
