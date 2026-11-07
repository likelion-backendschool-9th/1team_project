package com.dipsource.controller.report;

import com.dipsource.dto.report.ReportReasonRequest;
import com.dipsource.dto.report.ReportResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.report.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 신고 컨트롤러
 *
 * POST /feeds/{feedId}/report   — 피드 신고
 * POST /posts/{postId}/report   — 블로그 글 신고
 * POST /demos/{demoId}/report   — 데모 신고
 */
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** 피드 신고. 201 Created 반환 */
    @PostMapping("/feeds/{feedId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportFeed(
            @PathVariable Long feedId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "FEED", feedId, body.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 블로그 글 신고. 201 Created 반환 */
    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "POST", postId, body.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 데모 신고. 201 Created 반환 */
    @PostMapping("/demos/{demoId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportDemo(
            @PathVariable Long demoId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "DEMO", demoId, body.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }
}
