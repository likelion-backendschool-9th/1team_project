package com.dipsource.repository.demo;

import com.dipsource.entity.demo.Demo;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** demos 테이블 접근 */
public interface DemoRepository extends JpaRepository<Demo, Long> {

    long countByUserIdAndDeletedAtIsNull(Long userId);

    @Query("""
            SELECT d.userId, COUNT(d) FROM Demo d
            WHERE d.deletedAt IS NULL AND d.userId IN :userIds
            GROUP BY d.userId
            """)
    List<Object[]> countActiveByUserIds(@Param("userIds") Collection<Long> userIds);
}
