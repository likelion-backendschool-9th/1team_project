package com.dipsource.service.admin;

import com.dipsource.dto.admin.AdminCoinTxResponse;
import com.dipsource.dto.admin.AdminRevenueResponse;
import com.dipsource.dto.admin.AdminSubscriptionResponse;
import com.dipsource.global.common.PageResponse;
import java.time.YearMonth;
import java.util.List;
import org.springframework.data.domain.Pageable;

/** 관리자 결제 조회: 칩 충전 내역, 구독 목록, 매출. */
public interface AdminPaymentService {

    /** 칩 충전 내역. userId가 null이면 전체, type은 "charge"·"refunded"·null(둘 다) */
    PageResponse<AdminCoinTxResponse> listCharges(Long userId, String type, Pageable pageable);

    /** 블로그 구독 목록. 최근 시작한 순 */
    PageResponse<AdminSubscriptionResponse> listSubscriptions(Pageable pageable);

    /** from~to 달(둘 다 포함, "YYYY-MM")의 월별 매출. 비우면 이번 달까지 최근 6개월. 최대 24개월 */
    List<AdminRevenueResponse> revenue(String from, String to);

    /** 한 달 매출 합계(원). 대시보드용 */
    long revenueOf(YearMonth month);
}
