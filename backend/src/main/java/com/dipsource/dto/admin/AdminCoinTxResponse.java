package com.dipsource.dto.admin;

import com.dipsource.entity.coin.Payment;
import com.dipsource.entity.coin.PaymentStatus;
import java.time.format.DateTimeFormatter;

/**
 * 관리자 충전 내역 한 건 (프론트 CoinTx).
 *
 * @param type  "charge"(결제 완료) / "refunded"(환불됨). 회원 환불 요청 기능은 없어서 "refund-request"는 오지 않는다
 * @param chips 충전한 칩
 * @param memo  결제 금액 또는 환불 안내
 */
public record AdminCoinTxResponse(
        Long id,
        Long userId,
        String user,
        String type,
        int chips,
        int amountKrw,
        String createdAt,
        String memo
) {
    public static AdminCoinTxResponse of(Payment payment, String nickname) {
        boolean refunded = payment.getStatus() == PaymentStatus.REFUNDED;
        return new AdminCoinTxResponse(
                payment.getId(),
                payment.getUserId(),
                nickname,
                refunded ? "refunded" : "charge",
                payment.getChips() == null ? 0 : payment.getChips(),
                payment.getAmountKrw(),
                payment.getCreatedAt() == null ? null : payment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                refunded ? "환불 완료" : String.format("%,d원 결제", payment.getAmountKrw())
        );
    }
}
