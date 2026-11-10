package com.dipsource.controller.admin;

import com.dipsource.dto.admin.AdminSuspiciousResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.admin.AdminCoinService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 칩 충전 환불·이상 거래. ADMIN만 (SecurityConfig /admin/**). */
@RestController
@RequestMapping("/admin/coins")
@RequiredArgsConstructor
public class AdminCoinController {

    private final AdminCoinService adminCoinService;

    /** 칩 충전 환불. 충전한 칩을 회수하고 결제를 환불됨으로 바꾼다 */
    @PutMapping("/purchases/{paymentId}/refund")
    public ResponseEntity<Void> refund(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long paymentId) {
        adminCoinService.refund(AdminAuth.requireUserId(principal), paymentId);
        return ResponseEntity.noContent().build();
    }

    /** 이상 거래 목록 (조회할 때마다 계산) */
    @GetMapping("/suspicious")
    public ApiResponse<List<AdminSuspiciousResponse>> suspicious() {
        return ApiResponse.of(adminCoinService.findSuspicious());
    }
}
