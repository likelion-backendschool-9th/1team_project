package com.dipsource.repository.minihome;

import com.dipsource.entity.minihome.Guestbook;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** guestbooks 테이블 접근. */
public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {

    Page<Guestbook> findByHome_IdAndDeletedAtIsNull(Long homeId, Pageable pageable);

    @Query("SELECT g FROM Guestbook g JOIN FETCH g.home WHERE g.id IN :ids")
    List<Guestbook> findAllWithHomeByIdIn(@Param("ids") Collection<Long> ids);
}
