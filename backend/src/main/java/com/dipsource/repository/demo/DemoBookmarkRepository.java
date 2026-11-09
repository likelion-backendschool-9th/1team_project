package com.dipsource.repository.demo;

import com.dipsource.entity.demo.DemoBookmark;
import com.dipsource.entity.demo.DemoUserId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** bookmarks(데모 북마크) 테이블 접근 */
public interface DemoBookmarkRepository extends JpaRepository<DemoBookmark, DemoUserId> {

    @Query(value = """
            SELECT demo_id FROM bookmarks
            WHERE user_id = :userId AND demo_id IN (:demoIds)
            """, nativeQuery = true)
    List<Long> findBookmarkedDemoIds(@Param("userId") Long userId, @Param("demoIds") List<Long> demoIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO bookmarks (user_id, demo_id, created_at)
            VALUES (:userId, :demoId, NOW())
            ON CONFLICT (user_id, demo_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("demoId") Long demoId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM bookmarks WHERE user_id = :userId AND demo_id = :demoId", nativeQuery = true)
    int deleteByUserIdAndDemoId(@Param("userId") Long userId, @Param("demoId") Long demoId);
}
