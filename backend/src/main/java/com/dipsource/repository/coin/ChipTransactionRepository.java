package com.dipsource.repository.coin;

import com.dipsource.entity.coin.ChipTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** chip_transactions 테이블 접근 */
public interface ChipTransactionRepository extends JpaRepository<ChipTransaction, Long> {

    /** 한 회원의 거래 내역. 최신순 */
    Page<ChipTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
