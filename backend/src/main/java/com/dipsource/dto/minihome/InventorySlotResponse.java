package com.dipsource.dto.minihome;

/**
 * 데모 인벤토리 칸 구매 결과.
 *
 * @param extraInventorySlots 지금까지 늘린 칸 수
 * @param chipBalance         차감 후 남은 칩
 */
public record InventorySlotResponse(
        int extraInventorySlots,
        int chipBalance
) {
}
