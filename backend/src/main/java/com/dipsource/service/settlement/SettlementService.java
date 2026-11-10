package com.dipsource.service.settlement;

import com.dipsource.dto.settlement.MyDailyIncomeResponse;
import com.dipsource.dto.settlement.MyIncomeResponse;
import com.dipsource.dto.settlement.MyIncomeSourceResponse;
import com.dipsource.dto.settlement.MySettlementResponse;
import com.dipsource.dto.settlement.MySettlementSummaryResponse;
import com.dipsource.dto.settlement.SettlementGenerateResponse;
import com.dipsource.dto.settlement.SettlementResponse;
import com.dipsource.global.common.PageResponse;
import java.time.LocalDate;
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

    /** 로그인한 회원의 정산. 최신 월부터. page는 0부터, size는 1~20 */
    PageResponse<MySettlementResponse> mySettlements(Long userId, int page, int size);

    /** 이번 달 예상 수익과 확정 지급액 합계 */
    MySettlementSummaryResponse mySummary(Long userId);

    /**
     * 그 달의 날짜별 수익. 수익이 없는 날도 0으로 채운다.
     * 이번 달이면 오늘까지. 아직 오지 않은 달은 400.
     */
    List<MyDailyIncomeResponse> myDaily(Long userId, YearMonth month);

    /** 그 달 수익을 ChipRefType 별로 모은다. 글 판매·후원은 0이어도 포함한다 */
    List<MyIncomeSourceResponse> mySources(Long userId, YearMonth month);

    /**
     * 수익 내역. month와 date 중 하나만 있어야 한다.
     * 둘 다 없거나 둘 다 있으면 400. 이번 달은 오늘까지, 미래는 400.
     */
    List<MyIncomeResponse> myIncomes(Long userId, YearMonth month, LocalDate date);
}
