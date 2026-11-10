package com.dipsource.repository.demo;

import com.dipsource.entity.demo.DemoLike;
import com.dipsource.entity.demo.DemoUserId;
import java.time.LocalDateTime;
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

    /**
     * 기간 안에 좋아요를 받은 공개 데모.
     * 각 행은 [demoId, userId, title, likeCount, playCount]. 숨긴·삭제한 데모와 정지·탈퇴 회원은 빼는다.
     */
    @Query("""
            SELECT d.id, d.userId, d.title, COUNT(l), d.playCount
            FROM DemoLike l
            JOIN l.demo d
            JOIN User u ON u.id = d.userId
            WHERE l.createdAt >= :since
              AND d.status = com.dipsource.entity.demo.DemoStatus.ACTIVE
              AND d.deletedAt IS NULL
              AND u.status = com.dipsource.entity.user.enums.UserStatus.ACTIVE
            GROUP BY d.id, d.userId, d.title, d.playCount
            """)
    List<Object[]> rankByRecentLikes(@Param("since") LocalDateTime since);
}
