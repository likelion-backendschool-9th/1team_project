package com.dipsource.service.admin;

import com.dipsource.dto.admin.AdminSettlementResponse;
import com.dipsource.dto.admin.CreateSettlementsResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 창작자 정산. 판매·후원 수익 칩을 달 단위로 현금 지급 대상으로 만든다. 수수료 30%. */
public interface AdminSettlementService {

    /**
     * 한 달 정산을 작가별로 만든다. month가 비면 지난달. 이번 달·미래 달은 400.
     * 이미 정산이 있는 작가는 건너뛴다 (다시 눌러도 중복 생성 안 됨).
     */
    CreateSettlementsResponse create(Long adminId, String month);

    /** 정산 목록. status는 "pending"·"done"·null, month는 "YYYY-MM"·null */
    PageResponse<AdminSettlementResponse> list(String status, String month, Pageable pageable);

    /**
     * 정산 확정. 작가의 칩을 정산한 만큼 차감하고(SETTLEMENT) 작업 기록을 남긴다.
     * 이미 확정했으면 409, 작가의 칩이 모자라면(이미 써 버림) 400
     */
    void confirm(Long adminId, Long settlementId);
}
