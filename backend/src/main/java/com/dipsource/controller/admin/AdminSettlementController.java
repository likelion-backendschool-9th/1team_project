package com.dipsource.controller.admin;

import com.dipsource.dto.admin.AdminSettlementResponse;
import com.dipsource.dto.admin.CreateSettlementsRequest;
import com.dipsource.dto.admin.CreateSettlementsResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.service.admin.AdminSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 정산. ADMIN만 (SecurityConfig /admin/**). */
@RestController
@RequestMapping("/admin/settlements")
@RequiredArgsConstructor
public class AdminSettlementController {

    private final AdminSettlementService adminSettlementService;

    /** 정산 목록. ?status=pending|done&month=YYYY-MM */
    @GetMapping
    public ApiResponse<PageResponse<AdminSettlementResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String month,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.of(adminSettlementService.list(status, month, pageable));
    }

    /** 한 달 정산 만들기. body의 month가 비면 지난달. 이미 만든 작가는 건너뛴다 */
    @PostMapping
    public ApiResponse<CreateSettlementsResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) CreateSettlementsRequest request) {
        String month = request == null ? null : request.month();
        return ApiResponse.of(adminSettlementService.create(AdminAuth.requireUserId(principal), month));
    }

    /** 정산 확정. 작가의 칩을 정산한 만큼 차감한다 */
    @PutMapping("/{settlementId}/confirm")
    public ResponseEntity<Void> confirm(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long settlementId) {
        adminSettlementService.confirm(AdminAuth.requireUserId(principal), settlementId);
        return ResponseEntity.noContent().build();
    }
}
