package com.dipsource.service.settlement;

import com.dipsource.dto.settlement.SettlementGenerateResponse;
import com.dipsource.dto.settlement.SettlementResponse;
import java.time.YearMonth;
import java.util.List;

/**
 * 창작자 정산 (칩 → 현금).
 * 기간(월) 동안 받은 수익 칩(글 판매·후원)을 모아 정산을 만들고, 관리자가 확정하면 그만큼 칩을 차감한다.
 */
public interface SettlementService {

    /** 수수료율 30% (기획서·database-design.md) */
    int FEE_PERCENT = 30;

    /** 최근 정산 목록 (최대 100건) */
    List<SettlementResponse> list();

    /**
     * 한 달 치 정산을 만든다. 그 달에 수익 칩이 있는 회원마다 대기(PENDING) 정산 1건.
     * 이미 그 달 정산이 있는 회원은 건너뛴다. 아직 오지 않은 달은 400.
     */
    SettlementGenerateResponse generate(Long adminId, YearMonth period);

    /**
     * 정산을 확정한다. 회원 칩에서 정산 칩을 차감하고 거래 내역(SETTLEMENT)과 관리자 기록을 남긴다.
     * 이미 확정했으면 409, 회원 칩이 정산 칩보다 적으면(그사이 써 버림) 400.
     */
    void confirm(Long adminId, Long settlementId);
}
