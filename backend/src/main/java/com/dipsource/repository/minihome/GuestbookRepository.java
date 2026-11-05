package com.dipsource.repository.minihome;

import com.dipsource.entity.minihome.Guestbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {

    Page<Guestbook> findByHomeIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long homeId, Pageable pageable);
}
