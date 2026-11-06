package com.dipsource.controller.report;

import com.dipsource.dto.report.ReportCreateRequest;
import com.dipsource.dto.report.ReportResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.report.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 신고 컨트롤러
 *
 * POST /reports — 신고 생성 (인증 필요)
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** 신고 생성. 신고 시점에 대상 콘텐츠를 스냅샷으로 저장함. 201 Created 반환 */
    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }
}
