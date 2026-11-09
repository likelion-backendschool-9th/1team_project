package com.dipsource.controller.admin;

import com.dipsource.dto.admin.AdminCoinTxResponse;
import com.dipsource.dto.admin.AdminRevenueResponse;
import com.dipsource.dto.admin.AdminSubscriptionResponse;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.service.admin.AdminPaymentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 결제 조회. ADMIN만 (SecurityConfig /admin/**). */
@RestController
@RequiredArgsConstructor
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    /** 칩 충전 내역. ?userId=&type=charge|refunded */
    @GetMapping("/admin/coins/purchases")
    public ApiResponse<PageResponse<AdminCoinTxResponse>> charges(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.of(adminPaymentService.listCharges(userId, type, pageable));
    }

    /** 블로그 구독 목록 */
    @GetMapping("/admin/subscriptions")
    public ApiResponse<PageResponse<AdminSubscriptionResponse>> subscriptions(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.of(adminPaymentService.listSubscriptions(pageable));
    }

    /** 월별 매출. ?from=YYYY-MM&to=YYYY-MM (비우면 최근 6개월) */
    @GetMapping("/admin/revenue")
    public ApiResponse<List<AdminRevenueResponse>> revenue(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ApiResponse.of(adminPaymentService.revenue(from, to));
    }
}
