package com.dipsource.controller.minihome;

import com.dipsource.dto.minihome.InventorySlotResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.minihome.InventorySlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 데모 인벤토리 칸 구매 — POST /mini-homes/me/inventory-slots (10칩 차감, 1칸 추가) */
@RestController
@RequestMapping("/mini-homes/me/inventory-slots")
@RequiredArgsConstructor
public class InventorySlotController {

    private final InventorySlotService inventorySlotService;

    @PostMapping
    public ApiResponse<InventorySlotResponse> buy(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.of(inventorySlotService.buySlot(principal.getUserId()));
    }
}
