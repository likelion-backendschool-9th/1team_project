package com.dipsource.service.demo.mock;

import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Mock 설정과 샘플 데이터를 검증해 Worker가 읽을 형태로 만든다.
 * 등록된 정적 응답만 허용한다. 코드나 스크립트를 넣을 자리는 없다.
 */
@Component
public class DemoMockCompiler {

    public static final CompiledMock EMPTY = new CompiledMock(null, null, null);

    private static final int MAX_ENDPOINTS = 50;
    private static final int MAX_DEPTH = 16;
    private static final int PARSER_DEPTH = MAX_DEPTH + 8;
    private static final int MAX_COMPILED_BYTES = 512 * 1024;
    private static final int MAX_PATH_LENGTH = 200;
    private static final Set<String> METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE");
    private static final Set<String> ENDPOINT_FIELDS = Set.of("method", "path", "status", "response", "dataKey");
    private static final Pattern PATH = Pattern.compile("^(/[A-Za-z0-9_~-][A-Za-z0-9._~-]*)+$");

    private final ObjectMapper strictMapper = JsonMapper.builder(JsonFactory.builder()
                    .streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(PARSER_DEPTH).build())
                    .build())
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .build();

    /** @throws BusinessException 문법·형식·크기가 맞지 않을 때 (400) */
    public CompiledMock compile(String mockConfig, String sampleData) {
        String config = blankToNull(mockConfig);
        String sample = blankToNull(sampleData);
        JsonNode sampleRoot = sample == null ? null : parse(sample, "샘플 데이터");
        if (sampleRoot != null && !sampleRoot.isObject()) {
            throw invalid("샘플 데이터는 { \"이름\": 값 } 형태의 객체여야 합니다");
        }
        if (config == null) {
            return new CompiledMock(null, sample, null);
        }

        JsonNode root = parse(config, "Mock 설정");
        JsonNode endpoints = root.path("endpoints");
        if (!endpoints.isArray()) {
            throw invalid("Mock 설정에는 endpoints 배열이 있어야 합니다");
        }
        if (endpoints.size() > MAX_ENDPOINTS) {
            throw invalid("엔드포인트는 " + MAX_ENDPOINTS + "개까지 등록할 수 있습니다");
        }

        ArrayNode compiled = strictMapper.createArrayNode();
        Set<String> seen = new HashSet<>();
        for (JsonNode endpoint : endpoints) {
            compiled.add(compileEndpoint(endpoint, sampleRoot, seen));
        }
        ObjectNode result = strictMapper.createObjectNode();
        result.set("endpoints", compiled);
        byte[] json = result.toString().getBytes(StandardCharsets.UTF_8);
        if (json.length > MAX_COMPILED_BYTES) {
            throw invalid("Mock 응답 전체 크기는 512KB 이하여야 합니다");
        }
        return new CompiledMock(config, sample, json);
    }

    private ObjectNode compileEndpoint(JsonNode endpoint, JsonNode sampleRoot, Set<String> seen) {
        if (!endpoint.isObject()) {
            throw invalid("엔드포인트는 객체여야 합니다");
        }
        for (Iterator<String> names = endpoint.fieldNames(); names.hasNext(); ) {
            String name = names.next();
            if (!ENDPOINT_FIELDS.contains(name)) {
                throw invalid("지원하지 않는 항목입니다: " + name);
            }
        }
        String method = endpoint.path("method").asText("");
        if (!METHODS.contains(method)) {
            throw invalid("지원하지 않는 메서드입니다: " + method);
        }
        String path = endpoint.path("path").asText("");
        if (path.length() > MAX_PATH_LENGTH || !PATH.matcher(path).matches()) {
            throw invalid("경로는 /products 형태여야 합니다: " + path);
        }
        if (!seen.add(method + " " + path)) {
            throw invalid("중복된 엔드포인트입니다: " + method + " " + path);
        }
        int status = endpoint.has("status") ? endpoint.path("status").asInt(-1) : 200;
        if (!endpoint.path("status").isMissingNode() && !endpoint.path("status").isInt()) {
            throw invalid("status는 숫자여야 합니다");
        }
        if (!(status >= 200 && status <= 299) && !(status >= 400 && status <= 599)) {
            throw invalid("status는 200번대 또는 400·500번대만 쓸 수 있습니다: " + status);
        }

        ObjectNode compiled = strictMapper.createObjectNode();
        compiled.put("method", method);
        compiled.put("path", path);
        compiled.put("status", status);
        JsonNode body = resolveBody(endpoint, sampleRoot, path);
        if (status == 204) {
            if (body != null) {
                throw invalid("204 응답에는 본문을 넣을 수 없습니다: " + path);
            }
        } else if (body != null) {
            compiled.set("response", body);
        }
        return compiled;
    }

    private JsonNode resolveBody(JsonNode endpoint, JsonNode sampleRoot, String path) {
        boolean hasResponse = endpoint.has("response");
        boolean hasDataKey = endpoint.has("dataKey");
        if (hasResponse && hasDataKey) {
            throw invalid("response와 dataKey는 함께 쓸 수 없습니다: " + path);
        }
        if (hasResponse) {
            return endpoint.get("response");
        }
        if (!hasDataKey) {
            return null;
        }
        String key = endpoint.path("dataKey").asText("");
        if (sampleRoot == null || !sampleRoot.has(key)) {
            throw invalid("샘플 데이터에 없는 키입니다: " + key);
        }
        return sampleRoot.get(key);
    }

    private JsonNode parse(String text, String label) {
        JsonNode root;
        try {
            root = strictMapper.readTree(text);
        } catch (JsonProcessingException e) {
            throw invalid(label + " JSON 문법 오류" + locationOf(e));
        }
        if (root == null || root.isMissingNode()) {
            throw invalid(label + "가 비어 있습니다");
        }
        requireDepth(root, 1, label);
        return root;
    }

    private void requireDepth(JsonNode node, int depth, String label) {
        if (depth > MAX_DEPTH) {
            throw invalid(label + "의 중첩이 너무 깊습니다 (최대 " + MAX_DEPTH + "단계)");
        }
        for (JsonNode child : node) {
            if (child.isContainerNode()) {
                requireDepth(child, depth + 1, label);
            }
        }
    }

    private static String locationOf(JsonProcessingException e) {
        return e.getLocation() == null ? "" : " (" + e.getLocation().getLineNr() + "행 " + e.getLocation().getColumnNr() + "열)";
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text;
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.BAD_REQUEST, message);
    }
}
