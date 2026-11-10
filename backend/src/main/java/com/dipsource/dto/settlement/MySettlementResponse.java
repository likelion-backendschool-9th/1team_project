package com.dipsource.dto.settlement;

import com.dipsource.entity.settlement.Settlement;
import com.dipsource.entity.settlement.SettlementStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 내 정산 한 줄. 다른 회원 정보는 넣지 않는다.
 *
 * @param gross       총수익(원)
 * @param fee         수수료(원)
 * @param netAmount   지급액(원)
 * @param status      PENDING(대기) / CONFIRMED(확정)
 * @param confirmedAt 확정 시각. 대기면 없다
 */
public record MySettlementResponse(
        Long id,
        String period,
        int totalChips,
        int gross,
        int fee,
        int netAmount,
        SettlementStatus status,
        LocalDateTime confirmedAt
) {

    private static final DateTimeFormatter PERIOD = DateTimeFormatter.ofPattern("yyyy-MM");

    public static MySettlementResponse of(Settlement settlement) {
        return new MySettlementResponse(
                settlement.getId(),
                settlement.getPeriodStart().format(PERIOD),
                settlement.getTotalChips(),
                settlement.getAmountKrw(),
                settlement.getFee(),
                settlement.getNetAmount(),
                settlement.getStatus(),
                settlement.getConfirmedAt()
        );
    }
}
