package com.dipsource.repository.notification;

import com.dipsource.entity.notification.Notification;
import com.dipsource.entity.notification.NotificationType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** 내 알림. 최신순(같은 시각이면 ID 역순). */
    Page<Notification> findByReceiverIdOrderByCreatedAtDescIdDesc(Long receiverId, Pageable pageable);

    long countByReceiverIdAndReadFalse(Long receiverId);

    /**
     * 내 알림 중 서로 차단한 사람이 보낸 것은 뺀다. 최신순(같은 시각이면 ID 역순).
     * 차단 전에 만들어진 알림이 남아 눌러도 404만 뜨던 문제를 막는다. 차단을 풀면 다시 보인다.
     */
    @Query(value = """
            SELECT n FROM Notification n
            WHERE n.receiverId = :receiverId
              AND (n.actorId IS NULL OR n.actorId NOT IN (
                    SELECT b.id.blockedId FROM Block b WHERE b.id.blockerId = :receiverId))
              AND (n.actorId IS NULL OR n.actorId NOT IN (
                    SELECT b.id.blockerId FROM Block b WHERE b.id.blockedId = :receiverId))
            ORDER BY n.createdAt DESC, n.id DESC
            """,
            countQuery = """
            SELECT COUNT(n) FROM Notification n
            WHERE n.receiverId = :receiverId
              AND (n.actorId IS NULL OR n.actorId NOT IN (
                    SELECT b.id.blockedId FROM Block b WHERE b.id.blockerId = :receiverId))
              AND (n.actorId IS NULL OR n.actorId NOT IN (
                    SELECT b.id.blockerId FROM Block b WHERE b.id.blockedId = :receiverId))
            """)
    Page<Notification> findVisibleByReceiverId(@Param("receiverId") Long receiverId, Pageable pageable);

    /** 안 읽은 알림 수. 목록과 같이 서로 차단한 사람이 보낸 것은 뺀다 */
    @Query("""
            SELECT COUNT(n) FROM Notification n
            WHERE n.receiverId = :receiverId AND n.read = false
              AND (n.actorId IS NULL OR n.actorId NOT IN (
                    SELECT b.id.blockedId FROM Block b WHERE b.id.blockerId = :receiverId))
              AND (n.actorId IS NULL OR n.actorId NOT IN (
                    SELECT b.id.blockerId FROM Block b WHERE b.id.blockedId = :receiverId))
            """)
    long countVisibleUnread(@Param("receiverId") Long receiverId);

    /** 본인 알림만 찾는다. 남의 알림 ID면 비어 있다. */
    Optional<Notification> findByIdAndReceiverId(Long id, Long receiverId);

    /** 같은 사람이 보낸 같은 종류의 안 읽은 알림이 since 이후에 있는지 (알림 묶기용) */
    boolean existsByReceiverIdAndActorIdAndTypeAndReadFalseAndCreatedAtAfter(
            Long receiverId, Long actorId, NotificationType type, LocalDateTime since);
}
