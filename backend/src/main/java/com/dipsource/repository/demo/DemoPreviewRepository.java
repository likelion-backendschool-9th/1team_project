package com.dipsource.repository.demo;

import com.dipsource.entity.demo.DemoPreview;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** demo_previews 테이블 접근 */
public interface DemoPreviewRepository extends JpaRepository<DemoPreview, String> {

    long countByUserIdAndExpiresAtAfter(Long userId, LocalDateTime now);

    List<DemoPreview> findByExpiresAtLessThanEqualOrderByExpiresAtAsc(LocalDateTime now, Pageable pageable);
}
