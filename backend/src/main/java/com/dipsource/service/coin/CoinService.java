package com.dipsource.service.coin;

import com.dipsource.dto.coin.ChipHistoryResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 칩 충전·사용·거래 내역 서비스 */
public interface CoinService {

    /**
     * 패키지 번호로 칩을 충전함.
     * PG 연동 전에는 즉시 PAID 처리.
     * idempotencyKey가 있으면 중복 요청을 멱등하게 처리함 (추후 PG 주문 번호로 교체 예정)
     *
     * @param userId         충전하는 회원
     * @param packageId      패키지 번호 (1~4)
     * @param idempotencyKey 클라이언트 멱등 키 (선택)
     */
    void charge(Long userId, int packageId, String idempotencyKey);

    /**
     * 한 회원의 칩 거래 내역을 최신순으로 반환한다.
     *
     * @param userId   조회 대상 회원
     * @param pageable 페이지 정보
     */
    PageResponse<ChipHistoryResponse> getHistory(Long userId, Pageable pageable);
}
