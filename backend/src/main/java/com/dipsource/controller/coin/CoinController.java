package com.dipsource.controller.coin;

import com.dipsource.dto.coin.ChipChargeRequest;
import com.dipsource.dto.coin.ChipHistoryResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.coin.CoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 칩 충전·거래 내역 */
@RestController
@RequestMapping("/coins")
@RequiredArgsConstructor
public class CoinController {

    private final CoinService coinService;

    /** 칩 충전 (로그인 필수) */
    @PostMapping("/purchases")
    public ResponseEntity<Void> purchase(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChipChargeRequest request
    ) {
        coinService.charge(requireUserId(principal), request.packageId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /** 칩 거래 내역 (로그인 필수) */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<ChipHistoryResponse>>> history(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                coinService.getHistory(requireUserId(principal), pageable)));
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
