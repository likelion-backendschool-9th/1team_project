package com.dipsource.controller.demo;

import com.dipsource.dto.demo.DemoTemplateDetail;
import com.dipsource.dto.demo.DemoTemplateSummary;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.demo.DemoTemplateService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 데모 템플릿 컨트롤러
 *
 * GET /demo-templates        — 목록 (비로그인 가능)
 * GET /demo-templates/{key}  — 내용 (비로그인 가능)
 */
@RestController
@RequestMapping("/demo-templates")
@RequiredArgsConstructor
public class DemoTemplateController {

    private final DemoTemplateService templateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DemoTemplateSummary>>> list() {
        return ResponseEntity.ok(ApiResponse.of(templateService.list()));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<DemoTemplateDetail>> get(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.of(templateService.get(key)));
    }
}
