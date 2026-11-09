package com.dipsource.repository.report;

import com.dipsource.entity.report.Report;
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
}
