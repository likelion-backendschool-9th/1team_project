package com.dipsource.service.demo.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dipsource.global.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DemoMockCompilerTest {

    private final DemoMockCompiler compiler = new DemoMockCompiler();

    private static String endpoints(String body) {
        return "{\"endpoints\":[" + body + "]}";
    }

    private void assertInvalid(String config, String sample, String message) {
        assertThatThrownBy(() -> compiler.compile(config, sample))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(message);
    }

    private JsonNode compiled(String config, String sample) throws Exception {
        return new ObjectMapper().readTree(new String(compiler.compile(config, sample).json(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("둘 다 비어 있으면 Mock을 만들지 않는다")
    void empty() {
        CompiledMock mock = compiler.compile(" ", null);
        assertThat(mock.json()).isNull();
        assertThat(mock.mockConfig()).isNull();
        assertThat(mock.sampleData()).isNull();
    }

    @Test
    @DisplayName("샘플 데이터만 있어도 저장한다")
    void sampleOnly() {
        CompiledMock mock = compiler.compile(null, "{\"a\":1}");
        assertThat(mock.json()).isNull();
        assertThat(mock.sampleData()).isEqualTo("{\"a\":1}");
    }

    @Test
    @DisplayName("정적 응답을 컴파일한다")
    void compilesResponse() throws Exception {
        JsonNode root = compiled(endpoints("{\"method\":\"GET\",\"path\":\"/products\",\"status\":200,\"response\":[{\"id\":1}]}"), null);
        JsonNode endpoint = root.get("endpoints").get(0);
        assertThat(endpoint.get("method").asText()).isEqualTo("GET");
        assertThat(endpoint.get("response").get(0).get("id").asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("dataKey를 샘플 데이터로 연결한다")
    void resolvesDataKey() throws Exception {
        JsonNode endpoint = compiled(endpoints("{\"method\":\"GET\",\"path\":\"/p\",\"dataKey\":\"products\"}"),
                "{\"products\":[{\"id\":2}]}").get("endpoints").get(0);
        assertThat(endpoint.get("status").asInt()).isEqualTo(200);
        assertThat(endpoint.get("response").get(0).get("id").asInt()).isEqualTo(2);
        assertThat(endpoint.has("dataKey")).isFalse();
    }

    @Test
    @DisplayName("문법 오류는 위치를 알려 준다")
    void reportsSyntaxLocation() {
        assertInvalid("{\n  \"endpoints\": [,]\n}", null, "Mock 설정 JSON 문법 오류 (2행");
        assertInvalid(null, "{broken", "샘플 데이터 JSON 문법 오류");
    }

    @Test
    @DisplayName("중복 키를 거부한다")
    void rejectsDuplicateKeys() {
        assertInvalid(null, "{\"a\":1,\"a\":2}", "문법 오류");
    }

    @Test
    @DisplayName("샘플 데이터는 객체여야 한다")
    void sampleMustBeObject() {
        assertInvalid(null, "[1,2]", "객체여야");
    }

    @Test
    @DisplayName("중첩 깊이를 제한한다")
    void limitsDepth() {
        assertInvalid(null, "{\"a\":" + "[".repeat(20) + "]".repeat(20) + "}", "중첩이 너무 깊습니다");
        assertInvalid(null, "{\"a\":" + "[".repeat(2000) + "]".repeat(2000) + "}", "문법 오류");
    }

    @Test
    @DisplayName("endpoints가 없으면 거부한다")
    void requiresEndpoints() {
        assertInvalid("{}", null, "endpoints");
        assertInvalid("{\"endpoints\":{}}", null, "endpoints");
    }

    @Test
    @DisplayName("엔드포인트 수를 제한한다")
    void limitsEndpointCount() {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            body.append(i == 0 ? "" : ",").append("{\"method\":\"GET\",\"path\":\"/a").append(i).append("\"}");
        }
        assertInvalid(endpoints(body.toString()), null, "50개까지");
    }

    @Test
    @DisplayName("응답 전체 크기를 제한한다")
    void limitsCompiledSize() {
        String big = "x".repeat(600 * 1024);
        assertInvalid(endpoints("{\"method\":\"GET\",\"path\":\"/a\",\"response\":\"" + big + "\"}"), null, "512KB");
    }

    @ParameterizedTest
    @ValueSource(strings = {"HEAD", "OPTIONS", "TRACE", "get", ""})
    @DisplayName("허용하지 않는 메서드를 거부한다")
    void rejectsMethods(String method) {
        assertInvalid(endpoints("{\"method\":\"" + method + "\",\"path\":\"/a\"}"), null, "메서드");
    }

    @ParameterizedTest
    @ValueSource(strings = {"products", "/", "/a/../b", "/a//b", "/a b", "/a?x=1", "/%2e", "/.hidden", "/a\\\\b"})
    @DisplayName("경로 형식을 검사한다")
    void rejectsPaths(String path) {
        assertInvalid(endpoints("{\"method\":\"GET\",\"path\":\"" + path + "\"}"), null, "경로");
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 199, 300, 302, 399, 600, 0, -1})
    @DisplayName("리다이렉트 등 쓸 수 없는 상태 코드를 거부한다")
    void rejectsStatuses(int status) {
        assertInvalid(endpoints("{\"method\":\"GET\",\"path\":\"/a\",\"status\":" + status + "}"), null, "status");
    }

    @Test
    @DisplayName("status가 숫자가 아니면 거부한다")
    void rejectsNonNumericStatus() {
        assertInvalid(endpoints("{\"method\":\"GET\",\"path\":\"/a\",\"status\":\"200\"}"), null, "status");
    }

    @Test
    @DisplayName("204에는 본문을 넣을 수 없다")
    void noBodyFor204() {
        assertInvalid(endpoints("{\"method\":\"DELETE\",\"path\":\"/a\",\"status\":204,\"response\":{}}"), null, "204");
    }

    @Test
    @DisplayName("중복 엔드포인트를 거부한다")
    void rejectsDuplicates() {
        assertInvalid(endpoints("{\"method\":\"GET\",\"path\":\"/a\"},{\"method\":\"GET\",\"path\":\"/a\"}"), null, "중복");
    }

    @Test
    @DisplayName("같은 경로의 다른 메서드는 허용한다")
    void allowsDifferentMethods() throws Exception {
        assertThat(compiled(endpoints("{\"method\":\"GET\",\"path\":\"/a\"},{\"method\":\"POST\",\"path\":\"/a\"}"), null)
                .get("endpoints")).hasSize(2);
    }

    @Test
    @DisplayName("스크립트 같은 알 수 없는 항목을 거부한다")
    void rejectsUnknownFields() {
        assertInvalid(endpoints("{\"method\":\"GET\",\"path\":\"/a\",\"script\":\"process.exit()\"}"), null, "지원하지 않는 항목");
    }

    @Test
    @DisplayName("response와 dataKey를 함께 쓸 수 없고 없는 키는 거부한다")
    void validatesDataKey() {
        assertInvalid(endpoints("{\"method\":\"GET\",\"path\":\"/a\",\"response\":1,\"dataKey\":\"x\"}"), "{\"x\":1}", "함께 쓸 수 없습니다");
        assertInvalid(endpoints("{\"method\":\"GET\",\"path\":\"/a\",\"dataKey\":\"x\"}"), "{\"y\":1}", "없는 키");
        assertInvalid(endpoints("{\"method\":\"GET\",\"path\":\"/a\",\"dataKey\":\"x\"}"), null, "없는 키");
    }
}
