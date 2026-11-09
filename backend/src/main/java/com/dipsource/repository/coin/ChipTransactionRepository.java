package com.dipsource.repository.coin;

import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import com.dipsource.repository.settlement.IncomeSum;
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

    /** 기간 [from, to) 안에서 회원별 수익 칩 합계. 합계가 0보다 큰 회원만 (정산 생성용) */
    @Query("""
            SELECT new com.dipsource.repository.settlement.IncomeSum(t.userId, SUM(t.amount))
            FROM ChipTransaction t
            WHERE t.type IN :types AND t.createdAt >= :from AND t.createdAt < :to
            GROUP BY t.userId
            HAVING SUM(t.amount) > 0
            ORDER BY t.userId
            """)
    List<IncomeSum> sumIncomeByUser(@Param("types") List<ChipTransactionType> types,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);
}
