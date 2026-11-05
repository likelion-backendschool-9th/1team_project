package com.dipsource.repository.minihome;

import com.dipsource.entity.minihome.Guestbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** guestbooks 테이블 접근. */
public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {

    Page<Guestbook> findByHome_IdAndDeletedAtIsNull(Long homeId, Pageable pageable);
}
