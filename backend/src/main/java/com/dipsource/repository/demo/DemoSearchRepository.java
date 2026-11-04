package com.dipsource.repository.demo;

import com.dipsource.entity.demo.Demo;
import com.dipsource.global.common.LikePattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * 데모 검색 전용 조회. 검색 방식을 바꿀 때는 이 파일만 바꾼다.
 * 데모 CRUD용 리포지토리와 섞이지 않도록 따로 둔다.
 */
public interface DemoSearchRepository extends Repository<Demo, Long> {

    /** 1단계: LOWER() + LIKE 부분 일치 */
    default Page<Demo> searchByKeyword(String keyword, Pageable pageable) {
        return searchByLikePattern(LikePattern.contains(keyword), pageable);
    }

    /** 공개(ACTIVE)이면서 삭제되지 않은 데모 중 제목 또는 설명에 패턴이 들어 있는 데모. 최신순. */
    @Query(value = """
            SELECT d FROM Demo d
            WHERE d.status = com.dipsource.entity.demo.DemoStatus.ACTIVE
              AND d.deletedAt IS NULL
              AND (LOWER(d.title) LIKE LOWER(:pattern) ESCAPE '\\'
                   OR LOWER(d.description) LIKE LOWER(:pattern) ESCAPE '\\')
            ORDER BY d.createdAt DESC, d.id DESC
            """,
            countQuery = """
            SELECT COUNT(d) FROM Demo d
            WHERE d.status = com.dipsource.entity.demo.DemoStatus.ACTIVE
              AND d.deletedAt IS NULL
              AND (LOWER(d.title) LIKE LOWER(:pattern) ESCAPE '\\'
                   OR LOWER(d.description) LIKE LOWER(:pattern) ESCAPE '\\')
            """)
    Page<Demo> searchByLikePattern(@Param("pattern") String pattern, Pageable pageable);
}
