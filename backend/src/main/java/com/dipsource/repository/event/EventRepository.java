package com.dipsource.repository.event;

import com.dipsource.entity.event.Event;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** site_events 테이블 접근. 삭제된 행은 조회하지 않는다. */
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
            SELECT e FROM Event e
            WHERE e.deletedAt IS NULL
              AND (:status IS NULL
                OR (:status = 'upcoming' AND e.startsAt > :today)
                OR (:status = 'ongoing' AND e.startsAt <= :today AND e.endsAt >= :today)
                OR (:status = 'ended' AND e.endsAt < :today))
            """)
    Page<Event> findVisible(@Param("status") String status, @Param("today") LocalDate today, Pageable pageable);

    Optional<Event> findByIdAndDeletedAtIsNull(Long id);
}
