package com.dipsource.repository.tag;

import com.dipsource.entity.tag.Tag;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** tags 테이블 접근. */
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    /** ID 목록에 해당하는 태그 수 반환. 존재 여부 검증용 */
    long countByIdIn(Collection<Long> ids);

    /**
     * 공개 데모·글·피드에 많이 붙은 태그 이름.
     * 검색 기록이 없을 때 인기 검색어 자리에 쓴다.
     */
    @Query(nativeQuery = true, value = """
            SELECT t.name
            FROM tags t
            JOIN (
                SELECT dt.tag_id AS tag_id
                FROM demo_tags dt
                JOIN demos d ON d.id = dt.demo_id AND d.status = 'ACTIVE'
                UNION ALL
                SELECT pt.tag_id
                FROM post_tags pt
                JOIN posts p ON p.id = pt.post_id AND p.status = 'ACTIVE'
                UNION ALL
                SELECT ft.tag_id
                FROM feed_tags ft
                JOIN feeds f ON f.id = ft.feed_id AND f.status = 'ACTIVE' AND f.deleted_at IS NULL
            ) used ON used.tag_id = t.id
            GROUP BY t.id, t.name
            ORDER BY COUNT(*) DESC, t.name ASC
            LIMIT :limit
            """)
    List<String> findPopularNames(@Param("limit") int limit);
}
