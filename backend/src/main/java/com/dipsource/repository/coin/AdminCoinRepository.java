package com.dipsource.repository.coin;

import com.dipsource.entity.coin.Payment;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * 관리자 충전 환불·이상 거래 탐지용 쿼리. 기본 저장·조회는 PaymentRepository를 쓴다.
 * created_at은 서울 벽시계 시각으로 저장된다 (JpaAuditingConfig).
 */
public interface AdminCoinRepository extends Repository<Payment, Long> {

    /** 환불할 결제를 잠근다. 두 관리자가 동시에 환불해도 한 번만 처리된다 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") Long id);

    /**
     * 기간 안에 칩 충전(결제 완료·환불 포함)을 minCount번 이상 한 회원.
     * @return [회원 ID, 횟수, 금액 합(원), 마지막 시각]
     */
    @Query(value = """
            SELECT user_id, COUNT(*), SUM(amount_krw), MAX(created_at)
            FROM payments
            WHERE payment_type = 'CHIP_CHARGE' AND status IN ('PAID', 'REFUNDED') AND created_at >= :since
            GROUP BY user_id
            HAVING COUNT(*) >= :minCount
            """, nativeQuery = true)
    List<Object[]> findRapidChargers(@Param("since") LocalDateTime since, @Param("minCount") int minCount);

    /**
     * 기간 안에 칩 충전 환불을 minCount번 이상 받은 회원.
     * @return [회원 ID, 횟수, 금액 합(원), 마지막 시각]
     */
    @Query(value = """
            SELECT user_id, COUNT(*), SUM(amount_krw), MAX(created_at)
            FROM payments
            WHERE payment_type = 'CHIP_CHARGE' AND status = 'REFUNDED' AND created_at >= :since
            GROUP BY user_id
            HAVING COUNT(*) >= :minCount
            """, nativeQuery = true)
    List<Object[]> findRepeatedRefunds(@Param("since") LocalDateTime since, @Param("minCount") int minCount);

    /**
     * 기간 안에 같은 사람에게 minCount번 이상 후원한 (보낸 사람, 받은 사람) 쌍.
     * @return [보낸 회원 ID, 받은 회원 ID, 횟수, 칩 합, 마지막 시각]
     */
    @Query(value = """
            SELECT d.sender_id, h.user_id, COUNT(*), SUM(d.chips), MAX(d.created_at)
            FROM donations d JOIN mini_homes h ON h.id = d.mini_home_id
            WHERE d.created_at >= :since
            GROUP BY d.sender_id, h.user_id
            HAVING COUNT(*) >= :minCount
            """, nativeQuery = true)
    List<Object[]> findRepeatedDonations(@Param("since") LocalDateTime since, @Param("minCount") int minCount);
}
