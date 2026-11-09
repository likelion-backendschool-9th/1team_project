package com.dipsource.repository.demo;

import com.dipsource.entity.demo.DemoFeedbackLike;
import com.dipsource.entity.demo.DemoFeedbackLikeId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** demo_feedback_likes 테이블 접근 */
public interface DemoFeedbackLikeRepository extends JpaRepository<DemoFeedbackLike, DemoFeedbackLikeId> {

    @Query(value = """
            SELECT feedback_id FROM demo_feedback_likes
            WHERE user_id = :userId AND feedback_id IN (:feedbackIds)
            """, nativeQuery = true)
    List<Long> findLikedFeedbackIds(@Param("userId") Long userId, @Param("feedbackIds") List<Long> feedbackIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO demo_feedback_likes (user_id, feedback_id, created_at)
            VALUES (:userId, :feedbackId, NOW())
            ON CONFLICT (user_id, feedback_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("feedbackId") Long feedbackId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM demo_feedback_likes WHERE user_id = :userId AND feedback_id = :feedbackId", nativeQuery = true)
    int deleteByUserIdAndFeedbackId(@Param("userId") Long userId, @Param("feedbackId") Long feedbackId);
}
