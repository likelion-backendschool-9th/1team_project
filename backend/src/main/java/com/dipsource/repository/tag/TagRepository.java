package com.dipsource.repository.tag;

import com.dipsource.entity.tag.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** tags 테이블 접근. */
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);
}
