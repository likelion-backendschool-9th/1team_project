package com.dipsource.controller.admin;

import com.dipsource.dto.admin.AdminReportResponse;
import com.dipsource.dto.admin.HandleReportRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.service.admin.AdminReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    public ApiResponse<PageResponse<AdminReportResponse>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.of(adminReportService.list(type, status, pageable));
    }

    @GetMapping("/{reportId}")
    public ApiResponse<AdminReportResponse> get(@PathVariable Long reportId) {
        return ApiResponse.of(adminReportService.get(reportId));
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<Void> handle(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId,
            @Valid @RequestBody HandleReportRequest request) {
        adminReportService.handle(AdminAuth.requireUserId(principal), reportId, request.decision(), request.memo());
        return ResponseEntity.noContent().build();
    }
}
