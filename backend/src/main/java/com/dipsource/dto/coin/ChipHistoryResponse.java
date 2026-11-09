package com.dipsource.dto.coin;

import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import java.time.LocalDateTime;

/**
 * 칩 거래 내역 한 건 (프론트 ChipHistory).
 *
 * @param type "charge"(충전) / "use"(사용) / "refund"(환불·수입)
 */
public record ChipHistoryResponse(
        Long id,
        String type,
        String description,
        int amount,
        LocalDateTime createdAt
) {

    public static ChipHistoryResponse of(ChipTransaction tx) {
        return new ChipHistoryResponse(
                tx.getId(),
                toType(tx.getType()),
                tx.getDescription(),
                tx.getAmount(),
                tx.getCreatedAt()
        );
    }

    /** ChipTransactionType → 프론트 문자열 타입 변환 */
    private static String toType(ChipTransactionType type) {
        return switch (type) {
            case CHARGE -> "charge";
            case REFUND -> "refund";
            case BLOG_INCOME, DONATION_INCOME -> "revenue";
            default -> "use";
        };
    }
}
