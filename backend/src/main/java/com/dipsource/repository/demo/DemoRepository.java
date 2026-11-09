package com.dipsource.repository.demo;

import com.dipsource.entity.demo.Demo;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /** 관리자 데모 목록. 삭제(DELETED)만 빼고, 제목·설명·작성자 닉네임 검색. */
    @Query("""
            SELECT d FROM Demo d JOIN User u ON u.id = d.userId
            WHERE d.status <> com.dipsource.entity.demo.DemoStatus.DELETED
              AND (:q IS NULL OR LOWER(d.title) LIKE :q ESCAPE '\\'
                  OR LOWER(d.description) LIKE :q ESCAPE '\\'
                  OR LOWER(u.nickname) LIKE :q ESCAPE '\\')
            ORDER BY d.createdAt DESC, d.id DESC
            """)
    Page<Demo> searchForAdmin(@Param("q") String q, Pageable pageable);

    long countByDeletedAtIsNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            LocalDateTime from, LocalDateTime to);
}
