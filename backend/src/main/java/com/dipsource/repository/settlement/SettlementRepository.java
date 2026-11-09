package com.dipsource.repository.settlement;

import com.dipsource.entity.settlement.Settlement;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** settlements 테이블 접근 */
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    /** 최근 정산 100건. 기간 최신순, 같은 기간이면 ID 역순 */
    List<Settlement> findTop100ByOrderByPeriodStartDescIdDesc();

    /** 같은 회원·같은 기간 정산이 이미 있는지 (중복 생성 방지) */
    boolean existsByUserIdAndPeriodStart(Long userId, LocalDate periodStart);
}
