package com.dipsource.repository.notification;

import com.dipsource.entity.notification.Notification;
import com.dipsource.entity.notification.NotificationType;
import java.time.LocalDateTime;
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

    /** 같은 사람이 보낸 같은 종류의 안 읽은 알림이 since 이후에 있는지 (알림 묶기용) */
    boolean existsByReceiverIdAndActorIdAndTypeAndReadFalseAndCreatedAtAfter(
            Long receiverId, Long actorId, NotificationType type, LocalDateTime since);
}
