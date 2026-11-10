package com.dipsource.repository.coin;

import com.dipsource.entity.coin.ChipRefType;
import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import com.dipsource.repository.settlement.DailyIncome;
import com.dipsource.repository.settlement.IncomeSum;
import com.dipsource.repository.settlement.SourceIncome;
import java.time.LocalDateTime;
import java.util.Collection;
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

    /** 한 회원의 기간 [from, to) 수익 칩 합계. 거래가 없어도 0 */
    @Query("""
            SELECT new com.dipsource.repository.settlement.IncomeSum(
                :userId, COALESCE(SUM(t.amount), 0))
            FROM ChipTransaction t
            WHERE t.userId = :userId AND t.type IN :types
              AND t.createdAt >= :from AND t.createdAt < :to
            """)
    IncomeSum sumIncomeForUser(@Param("userId") Long userId,
                               @Param("types") List<ChipTransactionType> types,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    /** 한 회원의 기간 [from, to) 날짜별 수익. 수익이 있는 날만 온다 */
    @Query("""
            SELECT new com.dipsource.repository.settlement.DailyIncome(
                CAST(t.createdAt AS LocalDate), COALESCE(SUM(t.amount), 0), COUNT(t))
            FROM ChipTransaction t
            WHERE t.userId = :userId AND t.type IN :types
              AND t.createdAt >= :from AND t.createdAt < :to
            GROUP BY CAST(t.createdAt AS LocalDate)
            """)
    List<DailyIncome> sumIncomeByDay(@Param("userId") Long userId,
                                     @Param("types") List<ChipTransactionType> types,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    /** 한 회원의 기간 [from, to) 출처(ChipRefType)별 수익 */
    @Query("""
            SELECT new com.dipsource.repository.settlement.SourceIncome(
                t.refType, COALESCE(SUM(t.amount), 0), COUNT(t))
            FROM ChipTransaction t
            WHERE t.userId = :userId AND t.type IN :types
              AND t.createdAt >= :from AND t.createdAt < :to
            GROUP BY t.refType
            """)
    List<SourceIncome> sumIncomeBySource(@Param("userId") Long userId,
                                         @Param("types") List<ChipTransactionType> types,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);

    /** 한 회원의 기간 [from, to) 수익 거래. 최신순 */
    @Query("""
            SELECT t FROM ChipTransaction t
            WHERE t.userId = :userId AND t.type IN :types
              AND t.createdAt >= :from AND t.createdAt < :to
            ORDER BY t.createdAt DESC, t.id DESC
            """)
    List<ChipTransaction> findIncomeBetween(@Param("userId") Long userId,
                                            @Param("types") List<ChipTransactionType> types,
                                            @Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    /** 같은 원본을 가리키는 거래. 글 판매 수익의 구매자를 찾을 때 쓴다 */
    List<ChipTransaction> findByTypeAndRefTypeAndRefIdIn(ChipTransactionType type,
                                                         ChipRefType refType,
                                                         Collection<Long> refIds);
}
