package com.dipsource.repository.tag;

import com.dipsource.entity.tag.Tag;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** tags 테이블 접근. */
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    /** ID 목록에 해당하는 태그 수 반환. 존재 여부 검증용 */
    long countByIdIn(Collection<Long> ids);
}
