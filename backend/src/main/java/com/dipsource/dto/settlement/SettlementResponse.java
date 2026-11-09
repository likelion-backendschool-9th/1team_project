package com.dipsource.dto.settlement;

import com.dipsource.entity.settlement.Settlement;
import com.dipsource.entity.settlement.SettlementStatus;
import java.time.format.DateTimeFormatter;

/**
 * 관리자 정산 목록 한 줄. 프론트 admin/_lib/mock.ts의 Settlement와 같은 모양이다.
 *
 * @param creator    정산받는 회원 닉네임
 * @param gross      총수익(원) = 칩 x 칩 단가
 * @param fee        수수료(원)
 * @param status     pending(대기) / done(확정)
 * @param period     정산 기간. 예: 2026-10
 * @param totalChips 정산 대상 칩
 * @param netAmount  실지급액(원) = gross - fee
 */
public record SettlementResponse(
        Long id,
        String creator,
        int gross,
        int fee,
        String status,
        String period,
        int totalChips,
        int netAmount
) {

    private static final DateTimeFormatter PERIOD = DateTimeFormatter.ofPattern("yyyy-MM");

    public static SettlementResponse of(Settlement settlement, String creator) {
        return new SettlementResponse(
                settlement.getId(),
                creator,
                settlement.getAmountKrw(),
                settlement.getFee(),
                settlement.getStatus() == SettlementStatus.CONFIRMED ? "done" : "pending",
                settlement.getPeriodStart().format(PERIOD),
                settlement.getTotalChips(),
                settlement.getNetAmount()
        );
    }
}
