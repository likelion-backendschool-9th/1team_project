package com.dipsource.entity.search;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 색인 갱신 대기열(Transactional Outbox). 원본 변경과 같은 트랜잭션에 기록되므로 검색 엔진이 죽어도 갱신이 유실되지 않는다.
 * 행에는 "무엇이 바뀌었는지"만 있고 내용은 없다. 처리할 때 PostgreSQL의 현재 상태를 읽어서 색인하므로 중복 처리해도 안전하다.
 * 테이블: search_outbox
 */
@Getter
@Entity
@Table(name = "search_outbox", indexes = {
        @Index(name = "idx_search_outbox_pending", columnList = "processed_at, next_attempt_at"),
        @Index(name = "idx_search_outbox_created", columnList = "created_at"),
        @Index(name = "idx_search_outbox_target", columnList = "target_type, target_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** POST, DEMO, FEED, USER */
    @Column(name = "target_type", nullable = false, length = 10)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 처리 완료 시각. 재색인 중 변경분을 다시 적용할 수 있도록 바로 지우지 않고 일정 기간 남긴다 */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_error", length = 500)
    private String lastError;
}
