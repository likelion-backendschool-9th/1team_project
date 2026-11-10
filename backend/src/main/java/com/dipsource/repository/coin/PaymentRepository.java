package com.dipsource.repository.coin;

import com.dipsource.entity.coin.Payment;
import com.dipsource.entity.coin.PaymentStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** payments 테이블 접근 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** PG 주문 번호로 결제 조회. 멱등 키 중복 체크 및 PG 콜백 검증에 사용 */
    Optional<Payment> findByPgOrderId(String pgOrderId);

    /** 한 회원의 결제 내역. 최신순 */
    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 같은 유저가 기준 시각 이후에 충전한 결제가 있는지 확인.
     * 연타 방지(cooldown) 체크용
     */
    boolean existsByUserIdAndPaymentTypeAndCreatedAtAfter(
            Long userId,
            com.dipsource.entity.coin.PaymentType paymentType,
            java.time.LocalDateTime since);

    /**
     * 관리자 충전 내역. 결제 완료·환불된 칩 충전만, 최신순. userId가 null이면 전체
     */
    @Query("""
            SELECT p FROM Payment p
            WHERE p.paymentType = com.dipsource.entity.coin.PaymentType.CHIP_CHARGE
              AND p.status IN :statuses
              AND (:userId IS NULL OR p.userId = :userId)
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    Page<Payment> findChargesForAdmin(
            @Param("statuses") Collection<PaymentStatus> statuses,
            @Param("userId") Long userId,
            Pageable pageable);

    /**
     * 결제 완료(PAID)된 금액을 달·결제 종류별로 더한다. 환불된 결제는 빠진다.
     * created_at은 서울 벽시계 시각이라 그대로 달을 자른다.
     * @return [달("YYYY-MM"), 결제 종류(CHIP_CHARGE·BLOG_SUBSCRIPTION), 금액 합]
     */
    @Query(value = """
            SELECT to_char(created_at, 'YYYY-MM') AS month, payment_type, SUM(amount_krw)
            FROM payments
            WHERE status = 'PAID' AND created_at >= :from AND created_at < :to
            GROUP BY 1, 2
            """, nativeQuery = true)
    List<Object[]> sumPaidByMonth(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
