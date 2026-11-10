package com.dipsource.controller.settlement;

import com.dipsource.dto.settlement.MyDailyIncomeResponse;
import com.dipsource.dto.settlement.MyIncomeResponse;
import com.dipsource.dto.settlement.MyIncomeSourceResponse;
import com.dipsource.dto.settlement.MySettlementResponse;
import com.dipsource.dto.settlement.MySettlementSummaryResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.settlement.SettlementService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 작가가 자기 정산을 본다. userId는 로그인 정보에서만 가져온다. */
@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/me")
    public ApiResponse<PageResponse<MySettlementResponse>> mine(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.of(settlementService.mySettlements(requireUserId(principal), page, size));
    }

    @GetMapping("/me/summary")
    public ApiResponse<MySettlementSummaryResponse> summary(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.of(settlementService.mySummary(requireUserId(principal)));
    }

    @GetMapping("/me/daily")
    public ApiResponse<List<MyDailyIncomeResponse>> daily(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String month
    ) {
        return ApiResponse.of(settlementService.myDaily(requireUserId(principal), parseMonth(month)));
    }

    @GetMapping("/me/sources")
    public ApiResponse<List<MyIncomeSourceResponse>> sources(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String month
    ) {
        return ApiResponse.of(settlementService.mySources(requireUserId(principal), parseMonth(month)));
    }

    /** month=YYYY-MM 또는 date=YYYY-MM-DD 중 하나만 */
    @GetMapping("/me/incomes")
    public ApiResponse<List<MyIncomeResponse>> incomes(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String date
    ) {
        boolean hasMonth = month != null && !month.isBlank();
        boolean hasDate = date != null && !date.isBlank();
        if (hasMonth == hasDate) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "월 또는 날짜 중 하나만 골라 주세요.");
        }
        return ApiResponse.of(settlementService.myIncomes(
                requireUserId(principal),
                hasMonth ? parseMonth(month) : null,
                hasDate ? parseDate(date) : null
        ));
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }

    private static YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "월은 2026-10 형식으로 입력해 주세요.");
        }
    }

    private static LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date.trim());
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "날짜는 2026-10-31 형식으로 입력해 주세요.");
        }
    }
}
