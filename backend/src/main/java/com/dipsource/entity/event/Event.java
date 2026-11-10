package com.dipsource.entity.event;

import com.dipsource.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사이트 이벤트. 상태는 저장하지 않고 시작·종료일로 계산한다. 테이블: site_events */
@Getter
@Entity
@Table(name = "site_events", indexes = @Index(name = "idx_site_events_starts", columnList = "starts_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    /** 이벤트 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 제목 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 목록에 보이는 이모지. 없으면 🎉 */
    @Column(nullable = false, length = 8)
    private String emoji;

    /** 한 줄 소개 */
    @Column(nullable = false, length = 200)
    private String summary;

    /** 보상 */
    @Column(nullable = false, length = 100)
    private String reward;

    /** 시작일 (서울 기준 날짜) */
    @Column(name = "starts_at", nullable = false)
    private LocalDate startsAt;

    /** 종료일 (서울 기준 날짜, 당일 포함) */
    @Column(name = "ends_at", nullable = false)
    private LocalDate endsAt;

    /** 삭제 시각. 삭제하지 않았으면 null */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Event create(String title, String emoji, String summary, String reward,
                               LocalDate startsAt, LocalDate endsAt) {
        Event event = new Event();
        event.title = title;
        event.emoji = emoji;
        event.summary = summary;
        event.reward = reward;
        event.startsAt = startsAt;
        event.endsAt = endsAt;
        return event;
    }

    public void update(String title, String emoji, String summary, String reward,
                       LocalDate startsAt, LocalDate endsAt) {
        this.title = title;
        this.emoji = emoji;
        this.summary = summary;
        this.reward = reward;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
