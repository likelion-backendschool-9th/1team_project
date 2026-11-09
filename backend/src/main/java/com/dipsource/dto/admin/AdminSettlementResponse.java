package com.dipsource.dto.admin;

import com.dipsource.entity.settlement.Settlement;
import com.dipsource.entity.settlement.SettlementStatus;
import java.time.format.DateTimeFormatter;

/**
 * 관리자 정산 한 건 (프론트 Settlement).
 *
 * @param totalChips 정산 대상 칩 (판매·후원 수익)
 * @param gross      칩을 원으로 바꾼 금액
 * @param fee        수수료(원)
 * @param net        실지급액(원) = gross - fee
 * @param status     "pending"(대기) / "done"(확정)
 * @param period     정산 달 "YYYY-MM"
 */
public record AdminSettlementResponse(
        Long id,
        Long userId,
        String creator,
        int totalChips,
        int gross,
        int fee,
        int net,
        String status,
        String period,
        String confirmedAt
) {
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    public static AdminSettlementResponse of(Settlement settlement, String creator) {
        return new AdminSettlementResponse(
                settlement.getId(),
                settlement.getUserId(),
                creator,
                settlement.getTotalChips(),
                settlement.getAmountKrw(),
                settlement.getFee(),
                settlement.getNetAmount(),
                settlement.getStatus() == SettlementStatus.CONFIRMED ? "done" : "pending",
                settlement.getPeriodStart().format(MONTH),
                settlement.getConfirmedAt() == null ? null
                        : settlement.getConfirmedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
