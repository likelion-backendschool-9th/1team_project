package com.dipsource.repository.coin;

import com.dipsource.entity.coin.Payment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** payments 테이블 접근 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** PG 주문 번호로 결제 조회. PG 콜백 검증에 사용 */
    Optional<Payment> findByPgOrderId(String pgOrderId);

    /** 한 회원의 결제 내역. 최신순 */
    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
