package com.dipsource.repository.coin;

import com.dipsource.entity.coin.ChipTransaction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** chip_transactions 테이블 접근 */
public interface ChipTransactionRepository extends JpaRepository<ChipTransaction, Long> {

    /** 한 회원의 거래 내역. 최신순 */
    Page<ChipTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 기간 안의 판매·후원 수익 칩을 회원별로 더한다. 정산을 만들 때 쓴다.
     * created_at은 서울 벽시계 시각이다 (JpaAuditingConfig).
     * @return [회원 ID, 칩 합계]
     */
    @Query("""
            SELECT t.userId, SUM(t.amount) FROM ChipTransaction t
            WHERE t.type IN (com.dipsource.entity.coin.ChipTransactionType.BLOG_INCOME,
                             com.dipsource.entity.coin.ChipTransactionType.DONATION_INCOME)
              AND t.createdAt >= :from AND t.createdAt < :to
            GROUP BY t.userId
            """)
    List<Object[]> sumIncomeByUser(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
