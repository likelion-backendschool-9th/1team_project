package com.dipsource.repository.settlement;

import com.dipsource.entity.settlement.Settlement;
import com.dipsource.entity.settlement.SettlementStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** settlements 테이블 접근 */
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    /** 이 기간에 이미 정산을 만든 회원 ID */
    @Query("SELECT s.userId FROM Settlement s WHERE s.periodStart = :periodStart AND s.userId IN :userIds")
    List<Long> findUserIdsWithPeriod(
            @Param("periodStart") LocalDate periodStart,
            @Param("userIds") Collection<Long> userIds);

    /** 관리자 정산 목록. status·periodStart가 null이면 조건 없음. 최근 기간 → 금액 큰 순 */
    @Query("""
            SELECT s FROM Settlement s
            WHERE (:status IS NULL OR s.status = :status)
              AND (:periodStart IS NULL OR s.periodStart = :periodStart)
            ORDER BY s.periodStart DESC, s.amountKrw DESC, s.id DESC
            """)
    Page<Settlement> findForAdmin(
            @Param("status") SettlementStatus status,
            @Param("periodStart") LocalDate periodStart,
            Pageable pageable);
}
