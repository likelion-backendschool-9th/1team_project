package com.dipsource.dto.settlement;

import com.dipsource.entity.coin.ChipRefType;
import java.time.LocalDateTime;

/**
 * 수익 거래 한 건.
 *
 * @param occurredAt     거래 시각
 * @param refType        ChipRefType
 * @param label          수익 종류 라벨
 * @param targetType     대상 종류. POST / DONATION / PAYMENT / SETTLEMENT
 * @param targetId       대상 ID
 * @param title          대상 제목
 * @param senderNickname 보낸 사람 닉네임. 모르면 null
 * @param chips          수익 칩
 * @param gross          원화. 칩 x 칩 단가
 */
public record MyIncomeResponse(
        LocalDateTime occurredAt,
        ChipRefType refType,
        String label,
        String targetType,
        Long targetId,
        String title,
        String senderNickname,
        int chips,
        int gross
) {
}
