package com.dipsource.controller.demo;

import com.dipsource.global.common.ApiResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 데모 API — 아직 미구현이라 빈 응답만 반환 (프론트 에러 방지용) */
@RestController
@RequestMapping("/demos")
@RequiredArgsConstructor
public class DemoController {

    /** 데모 목록 조회 (임시: 빈 페이지 반환) */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> list() {
        return ResponseEntity.ok(ApiResponse.of(Map.of(
                "content", List.of(),
                "page", 0,
                "size", 0,
                "totalElements", 0,
                "totalPages", 0
        )));
    }
}
