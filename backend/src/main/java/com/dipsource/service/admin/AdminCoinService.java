package com.dipsource.service.admin;

import com.dipsource.dto.admin.AdminSuspiciousResponse;
import java.util.List;

/** 관리자 칩 충전 환불과 이상 거래 탐지. */
public interface AdminCoinService {

    /**
     * 칩 충전 환불. 결제를 REFUNDED로 바꾸고 충전한 칩을 회수한다(REFUND 거래 기록).
     * 칩 충전만 가능(400), 이미 환불했으면 409, 회원이 칩을 이미 써서 모자라면 400.
     * 실제 돈 돌려주기는 PG 연동 후에 붙인다 (지금은 기록만 바뀐다).
     */
    void refund(Long adminId, Long paymentId);

    /**
     * 이상 거래 목록. 규칙 3개를 조회할 때마다 계산한다 (저장하지 않음).
     * 24시간 안 충전 5회 이상 / 30일 안 충전 환불 2회 이상 / 7일 안 같은 사람에게 후원 5회 이상
     */
    List<AdminSuspiciousResponse> findSuspicious();
}
