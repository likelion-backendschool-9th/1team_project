package com.dipsource.repository.notification;

import com.dipsource.entity.notification.Notification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** 내 알림. 최신순(같은 시각이면 ID 역순). */
    Page<Notification> findByReceiverIdOrderByCreatedAtDescIdDesc(Long receiverId, Pageable pageable);

    long countByReceiverIdAndReadFalse(Long receiverId);

    /** 본인 알림만 찾는다. 남의 알림 ID면 비어 있다. */
    Optional<Notification> findByIdAndReceiverId(Long id, Long receiverId);
}
