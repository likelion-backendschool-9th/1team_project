package com.dipsource.repository.minihome;

import com.dipsource.entity.minihome.MiniHomeComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiniHomeCommentRepository extends JpaRepository<MiniHomeComment, Long> {

    Page<MiniHomeComment> findByHomeIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long homeId, Pageable pageable);
}
