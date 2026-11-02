package com.dipsource.entity.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 알림. 대상은 여러 기능에 걸쳐 있어 종류(target_type)와 ID로만 가리킨다. 테이블: notifications */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notifications",
        indexes = @Index(name = "idx_notifications_receiver_created", columnList = "receiver_id, created_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    /** 알림 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 받는 회원 ID(users.id) */
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    /** 알림을 만든 회원 ID(users.id). 시스템 알림이면 null */
    @Column(name = "actor_id")
    private Long actorId;

    /** 알림 종류 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    /** 대상 종류. 예: FEED, POST, DEMO, MINI_HOME */
    @Column(name = "target_type", length = 20)
    private String targetType;

    /** 대상 ID */
    @Column(name = "target_id")
    private Long targetId;

    /** 읽음 여부 */
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    /** 알림 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Notification(Long receiverId, Long actorId, NotificationType type,
                         String targetType, Long targetId) {
        this.receiverId = receiverId;
        this.actorId = actorId;
        this.type = type;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public void markRead() {
        this.read = true;
    }
}
