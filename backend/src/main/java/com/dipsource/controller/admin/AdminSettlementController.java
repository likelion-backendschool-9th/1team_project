package com.dipsource.controller.admin;

import com.dipsource.dto.settlement.SettlementGenerateResponse;
import com.dipsource.dto.settlement.SettlementResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.settlement.SettlementService;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 정산. /admin/** 는 SecurityConfig에서 관리자만 허용한다. */
@RestController
@RequestMapping("/admin/settlements")
@RequiredArgsConstructor
public class AdminSettlementController {

    private final SettlementService settlementService;

    @GetMapping
    public ApiResponse<List<SettlementResponse>> list() {
        return ApiResponse.of(settlementService.list());
    }

    /** period=2026-10 처럼 월을 받아 그 달 정산을 만든다 */
    @PostMapping
    public ApiResponse<SettlementGenerateResponse> generate(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String period
    ) {
        return ApiResponse.of(settlementService.generate(AdminAuth.requireUserId(principal), parse(period)));
    }

    @PutMapping("/{settlementId}/confirm")
    public ResponseEntity<Void> confirm(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long settlementId
    ) {
        settlementService.confirm(AdminAuth.requireUserId(principal), settlementId);
        return ResponseEntity.noContent().build();
    }

    private static YearMonth parse(String period) {
        try {
            return YearMonth.parse(period.trim());
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "정산 기간은 2026-10 형식으로 입력해 주세요.");
        }
    }
}
