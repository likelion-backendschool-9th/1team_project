package com.dipsource.entity.event;

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

/** 이벤트(공지·챌린지). 관리자가 등록한다. 테이블: events */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "events",
        indexes = @Index(name = "idx_events_status_starts", columnList = "status, starts_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    /** 이벤트 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 등록한 관리자 ID(admins.id) */
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    /** 제목 */
    @Column(nullable = false, length = 200)
    private String title;

    /** 본문 */
    @Column(nullable = false, length = 5000)
    private String content;

    /** 썸네일 이미지 URL */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /** 시작 시각 */
    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    /** 종료 시각 */
    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.SCHEDULED;

    /** 등록 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 시각. 수정한 적 없으면 null */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 삭제 시각. 삭제하지 않았으면 null */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Event(Long adminId, String title, String content, String thumbnailUrl,
                  LocalDateTime startsAt, LocalDateTime endsAt) {
        this.adminId = adminId;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public void update(String title, String content, String thumbnailUrl,
                       LocalDateTime startsAt, LocalDateTime endsAt) {
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(EventStatus status) {
        this.status = status;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
