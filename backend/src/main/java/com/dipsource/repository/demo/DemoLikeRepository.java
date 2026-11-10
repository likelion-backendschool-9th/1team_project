package com.dipsource.repository.demo;

import com.dipsource.entity.demo.DemoLike;
import com.dipsource.entity.demo.DemoUserId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** demo_likes 테이블 접근 */
public interface DemoLikeRepository extends JpaRepository<DemoLike, DemoUserId> {

    @Query(value = """
            SELECT demo_id FROM demo_likes
            WHERE user_id = :userId AND demo_id IN (:demoIds)
            """, nativeQuery = true)
    List<Long> findLikedDemoIds(@Param("userId") Long userId, @Param("demoIds") List<Long> demoIds);

    /** @return 삽입된 행 수 (0이면 이미 좋아요됨) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO demo_likes (user_id, demo_id, created_at)
            VALUES (:userId, :demoId, NOW())
            ON CONFLICT (user_id, demo_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("demoId") Long demoId);

    /** @return 삭제된 행 수 (0이면 이미 없음) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM demo_likes WHERE user_id = :userId AND demo_id = :demoId", nativeQuery = true)
    int deleteByUserIdAndDemoId(@Param("userId") Long userId, @Param("demoId") Long demoId);
}
