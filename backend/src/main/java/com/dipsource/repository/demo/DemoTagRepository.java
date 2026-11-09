package com.dipsource.repository.demo;

import com.dipsource.entity.demo.DemoTag;
import com.dipsource.entity.demo.DemoTagId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** demo_tags 테이블 접근 */
public interface DemoTagRepository extends JpaRepository<DemoTag, DemoTagId> {

    /** 여러 데모의 태그 이름. [demoId, name] 목록 (N+1 방지) */
    @Query("""
            SELECT dt.id.demoId, t.name FROM DemoTag dt JOIN Tag t ON t.id = dt.id.tagId
            WHERE dt.id.demoId IN :demoIds
            ORDER BY t.name
            """)
    List<Object[]> findTagNames(@Param("demoIds") Collection<Long> demoIds);

    /** 데모의 태그 연결을 모두 지운다 (수정할 때 다시 붙인다). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM DemoTag dt WHERE dt.id.demoId = :demoId")
    int deleteByDemoId(@Param("demoId") Long demoId);
}
