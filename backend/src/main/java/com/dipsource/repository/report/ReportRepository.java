package com.dipsource.repository.report;

import com.dipsource.entity.report.Report;
import com.dipsource.entity.report.ReportStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    /** 같은 대상에 이미 신고했는지 확인 */
    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, String targetType, Long targetId);

    long countByTargetTypeAndTargetId(String targetType, Long targetId);

    @Query("""
            SELECT r.targetId, COUNT(r) FROM Report r
            WHERE r.targetType = :targetType AND r.targetId IN :targetIds
            GROUP BY r.targetId
            """)
    List<Object[]> countByTargetTypeAndTargetIds(
            @Param("targetType") String targetType,
            @Param("targetIds") Collection<Long> targetIds);

    /** 대상별 신고 건수. targetIds가 비어 있으면 호출하지 않는다. */
    @Query("""
            SELECT r.targetId AS targetId, COUNT(r) AS cnt
            FROM Report r
            WHERE r.targetType = :targetType
            AND r.targetId IN :targetIds
            GROUP BY r.targetId
            """)
    List<TargetCount> countByTargets(
            @Param("targetType") String targetType,
            @Param("targetIds") Collection<Long> targetIds);

    interface TargetCount {
        Long getTargetId();
        long getCnt();
    }

    /** 관리자 신고 목록. 대기(PENDING)를 앞에 두고 최신순. */
    @Query("""
            SELECT r FROM Report r
            WHERE (:status IS NULL OR r.status = :status)
              AND (:targetType IS NULL OR r.targetType = :targetType)
            ORDER BY CASE WHEN r.status = com.dipsource.entity.report.ReportStatus.PENDING THEN 0
                          WHEN r.status = com.dipsource.entity.report.ReportStatus.RESOLVED THEN 1
                          ELSE 2 END,
                     r.createdAt DESC, r.id DESC
            """)
    List<Report> searchForAdmin(
            @Param("status") ReportStatus status,
            @Param("targetType") String targetType);
}
