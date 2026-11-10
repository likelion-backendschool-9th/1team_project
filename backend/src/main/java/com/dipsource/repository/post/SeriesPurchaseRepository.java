package com.dipsource.repository.post;

import com.dipsource.entity.post.SeriesPurchase;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** series_purchases 테이블 접근 */
public interface SeriesPurchaseRepository extends JpaRepository<SeriesPurchase, Long> {

    boolean existsByUserIdAndSeriesId(Long userId, Long seriesId);

    boolean existsBySeriesId(Long seriesId);

    /** 시리즈를 산 회원 ID. 새 회차 알림에 쓴다 */
    @Query("SELECT p.userId FROM SeriesPurchase p WHERE p.seriesId = :seriesId")
    List<Long> findUserIdsBySeriesId(@Param("seriesId") Long seriesId);

    /** 내가 산 시리즈. 최근에 산 순 */
    List<SeriesPurchase> findByUserIdOrderByPurchasedAtDescIdDesc(Long userId);

    /** 시리즈별 구매 수. [seriesId, 수] */
    @Query("SELECT p.seriesId, COUNT(p) FROM SeriesPurchase p WHERE p.seriesId IN :seriesIds GROUP BY p.seriesId")
    List<Object[]> countBySeriesIds(@Param("seriesIds") Collection<Long> seriesIds);

    /**
     * 구매 기록 저장. 이미 산 시리즈면 무시한다.
     * 같은 구매가 동시에 들어오면 두 번째 요청은 첫 요청이 끝날 때까지 기다렸다가 0을 받는다.
     * @return 0이면 이미 구매, 1이면 새로 구매
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO series_purchases (user_id, series_id, paid_price, purchased_at)
            VALUES (:userId, :seriesId, :paidPrice, NOW())
            ON CONFLICT (user_id, series_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("seriesId") Long seriesId, @Param("paidPrice") int paidPrice);
}
