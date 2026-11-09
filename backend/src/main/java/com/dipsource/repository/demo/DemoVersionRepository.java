package com.dipsource.repository.demo;

import com.dipsource.entity.demo.DemoThumbnailStatus;
import com.dipsource.entity.demo.DemoVersion;
import com.dipsource.entity.demo.DemoVersionStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** demo_versions 테이블 접근 */
public interface DemoVersionRepository extends JpaRepository<DemoVersion, Long> {

    Optional<DemoVersion> findByIdAndDemoId(Long id, Long demoId);

    long countByDemoId(Long demoId);

    /** 지워지지 않은 버전 수. 보관 개수 제한에 쓴다 */
    long countByDemoIdAndStatusIn(Long demoId, Collection<DemoVersionStatus> statuses);

    /** 관리 화면에 보여 줄 버전. 최신순 */
    List<DemoVersion> findByDemoIdAndStatusInOrderByIdDesc(Long demoId, Collection<DemoVersionStatus> statuses);

    /** 촬영할 차례가 된 버전 하나 */
    Optional<DemoVersion> findFirstByStatusAndThumbnailStatusAndThumbnailNextAtLessThanEqualOrderByThumbnailNextAtAsc(
            DemoVersionStatus status, DemoThumbnailStatus thumbnailStatus, LocalDateTime now);

    Optional<DemoVersion> findFirstByDemoIdAndStatusOrderByIdDesc(Long demoId, DemoVersionStatus status);

    /**
     * 파일을 지워야 하는 버전: 저장 실패, 오래 멈춘 업로드, 삭제된 데모의 버전.
     * 파일 삭제가 성공해야 DELETED가 되므로 R2 장애로 실패해도 다음 주기에 다시 잡힌다.
     */
    @Query("""
            SELECT v FROM DemoVersion v
            WHERE v.status <> com.dipsource.entity.demo.DemoVersionStatus.DELETED
              AND (v.status = com.dipsource.entity.demo.DemoVersionStatus.FAILED
                   OR v.status = com.dipsource.entity.demo.DemoVersionStatus.DELETING
                   OR (v.status = com.dipsource.entity.demo.DemoVersionStatus.UPLOADING AND v.updatedAt < :stuckBefore)
                   OR EXISTS (SELECT 1 FROM Demo d WHERE d.id = v.demoId AND d.deletedAt IS NOT NULL))
            ORDER BY v.id
            """)
    List<DemoVersion> findPurgeTargets(@Param("stuckBefore") LocalDateTime stuckBefore, Pageable pageable);
}
