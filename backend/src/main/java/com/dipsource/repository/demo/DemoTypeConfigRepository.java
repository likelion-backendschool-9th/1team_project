package com.dipsource.repository.demo;

import com.dipsource.entity.demo.DemoTypeConfig;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** demo_type_config 테이블 접근. 헬스체크는 외부 요청 동안 트랜잭션을 잡지 않도록 짧은 UPDATE로 나눈다. */
public interface DemoTypeConfigRepository extends JpaRepository<DemoTypeConfig, Long> {

    Optional<DemoTypeConfig> findByDemoId(Long demoId);

    List<DemoTypeConfig> findByDemoIdIn(Collection<Long> demoIds);

    /** 최소 간격이 지났을 때만 이번 확인 권한을 얻는다. 동시 요청 중 하나만 1을 받는다. */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE demo_type_config SET health_claimed_at = :now
            WHERE demo_id = :demoId AND health_check_url = :url
              AND (health_claimed_at IS NULL OR health_claimed_at <= :threshold)
            """, nativeQuery = true)
    int claimHealthCheck(@Param("demoId") Long demoId, @Param("url") String url,
                         @Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold);

    /** 확인하는 동안 URL이 바뀌었으면 옛 주소의 결과를 저장하지 않는다. */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE demo_type_config SET is_healthy = :healthy, last_health_at = :now
            WHERE demo_id = :demoId AND health_check_url = :url
            """, nativeQuery = true)
    int recordHealthResult(@Param("demoId") Long demoId, @Param("url") String url,
                           @Param("healthy") boolean healthy, @Param("now") LocalDateTime now);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE demo_type_config SET last_health_at = NULL
            WHERE demo_id = :demoId AND health_check_url = :url
            """, nativeQuery = true)
    int clearHealthResult(@Param("demoId") Long demoId, @Param("url") String url);
}
