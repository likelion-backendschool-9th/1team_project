package com.dipsource.repository.minihome;

import com.dipsource.entity.minihome.MiniHomeComment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** mini_home_comments 테이블 접근. */
public interface MiniHomeCommentRepository extends JpaRepository<MiniHomeComment, Long> {

    Page<MiniHomeComment> findByHome_IdAndDeletedAtIsNull(Long homeId, Pageable pageable);

    /** 지워진 글도 포함한다. 한 사람당 한 줄을 다시 쓰기 위해서다. */
    Optional<MiniHomeComment> findByHome_IdAndWriterId(Long homeId, Long writerId);
}
