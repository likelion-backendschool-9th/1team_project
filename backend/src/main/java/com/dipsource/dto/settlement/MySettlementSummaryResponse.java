package com.dipsource.dto.settlement;

/**
 * 내 정산 요약.
 *
 * @param period        이번 달. 예: 2026-11
 * @param expectedGross 아직 정산 행이 없는 이번 달 수익(원)
 * @param expectedFee   예상 수수료(원)
 * @param expectedNet   예상 지급액(원)
 * @param feePercent    수수료율
 * @param paidTotal     확정된 정산의 지급액 합(원)
 */
public record MySettlementSummaryResponse(
        String period,
        int expectedChips,
        int expectedGross,
        int expectedFee,
        int expectedNet,
        int feePercent,
        int paidTotal
) {
}
