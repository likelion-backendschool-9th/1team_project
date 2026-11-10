package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoTemplateDetail;
import com.dipsource.dto.demo.DemoTemplateSummary;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/** 찍먹이 관리하는 데모 템플릿. 저장소의 고정 파일만 제공하고 사용자가 등록할 수 없다. */
@Service
@RequiredArgsConstructor
public class DemoTemplateService {

    private static final List<String> KEYS = List.of("calculator", "drag-drop", "mini-game");

    private final ObjectMapper objectMapper;
    private volatile Map<String, DemoTemplateDetail> templates;

    public List<DemoTemplateSummary> list() {
        return load().values().stream()
                .map(t -> new DemoTemplateSummary(t.key(), t.title(), t.description(), t.usage()))
                .toList();
    }

    public DemoTemplateDetail get(String key) {
        DemoTemplateDetail template = load().get(key);
        if (template == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "템플릿을 찾을 수 없습니다");
        }
        return template;
    }

    private Map<String, DemoTemplateDetail> load() {
        if (templates == null) {
            Map<String, DemoTemplateDetail> loaded = new LinkedHashMap<>();
            KEYS.forEach(key -> loaded.put(key, read(key)));
            templates = loaded;
        }
        return templates;
    }

    private DemoTemplateDetail read(String key) {
        try {
            JsonNode meta = objectMapper.readTree(text(key, "meta.json"));
            return new DemoTemplateDetail(key, meta.path("title").asText(), meta.path("description").asText(),
                    meta.path("usage").asText(), text(key, "index.html"), text(key, "style.css"), text(key, "script.js"),
                    text(key, "mock.json"), text(key, "sample.json"));
        } catch (IOException e) {
            throw new IllegalStateException("템플릿을 읽지 못했습니다: " + key, e);
        }
    }

    private String text(String key, String file) throws IOException {
        try (InputStream in = new ClassPathResource("demo-templates/" + key + "/" + file).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
