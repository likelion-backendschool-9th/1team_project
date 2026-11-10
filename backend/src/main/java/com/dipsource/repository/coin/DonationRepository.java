package com.dipsource.repository.coin;

import com.dipsource.entity.coin.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

/** donations 테이블 접근 */
public interface DonationRepository extends JpaRepository<Donation, Long> {
}
