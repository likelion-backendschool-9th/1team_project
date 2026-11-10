package com.dipsource.repository.settlement;

import com.dipsource.entity.settlement.Settlement;
import com.dipsource.entity.settlement.SettlementStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** settlements 테이블 접근 */
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    /** 최근 정산 100건. 기간 최신순, 같은 기간이면 ID 역순 */
    List<Settlement> findTop100ByOrderByPeriodStartDescIdDesc();

    /** 같은 회원·같은 기간 정산이 이미 있는지 (중복 생성 방지) */
    boolean existsByUserIdAndPeriodStart(Long userId, LocalDate periodStart);

    /** 한 회원의 정산. 기간 최신순, 같은 기간이면 ID 역순 */
    Page<Settlement> findByUserIdOrderByPeriodStartDescIdDesc(Long userId, Pageable pageable);

    /** 확정된 정산의 실지급액 합. 없으면 0 */
    @Query("""
            SELECT COALESCE(SUM(s.netAmount), 0)
            FROM Settlement s
            WHERE s.userId = :userId AND s.status = :status
            """)
    long sumNetAmountByUserIdAndStatus(@Param("userId") Long userId, @Param("status") SettlementStatus status);
}
