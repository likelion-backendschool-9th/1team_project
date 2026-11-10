package com.dipsource.repository.post;

import com.dipsource.entity.post.PostSeries;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** post_series 테이블 접근 */
public interface PostSeriesRepository extends JpaRepository<PostSeries, Long> {

    /** 작가의 시리즈. 최근에 만든 순 */
    List<PostSeries> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    /**
     * 인기 시리즈. 구매가 많은 순, 같으면 최근에 만든 순.
     * 보는 사람과 차단 관계인 작가의 시리즈는 뺀다 (viewerId가 null이면 전부).
     */
    @Query("""
            SELECT s FROM PostSeries s
            LEFT JOIN SeriesPurchase p ON p.seriesId = s.id
            WHERE (:viewerId IS NULL OR (
                    s.userId NOT IN (SELECT b.id.blockedId FROM Block b WHERE b.id.blockerId = :viewerId)
                AND s.userId NOT IN (SELECT b.id.blockerId FROM Block b WHERE b.id.blockedId = :viewerId)))
            GROUP BY s
            ORDER BY COUNT(p) DESC, s.createdAt DESC, s.id DESC
            """)
    List<PostSeries> findPopular(@Param("viewerId") Long viewerId, Pageable pageable);
}
