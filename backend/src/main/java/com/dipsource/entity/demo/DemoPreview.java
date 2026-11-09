package com.dipsource.entity.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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

/** 공개 전에 확인하려고 잠시 올린 파일 묶음. 만료되면 파일과 함께 지운다. 테이블: demo_previews */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "demo_previews", indexes = {
        @Index(name = "idx_demo_previews_user", columnList = "user_id, expires_at"),
        @Index(name = "idx_demo_previews_expires", columnList = "expires_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoPreview {

    /** 미리보기 ID(UUID). R2 경로에도 쓴다 */
    @Id
    @Column(length = 36)
    private String id;

    /** 올린 회원 ID(users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 사용자 용량에 잡힌 크기(바이트) */
    @Column(name = "total_bytes", nullable = false)
    private long totalBytes;

    /** 만료 시각 */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** 업로드가 끝났는지. false로 남아 만료된 행은 예약 용량을 돌려준다 */
    @Column(nullable = false)
    private boolean completed;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private DemoPreview(String id, Long userId, long totalBytes, LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.totalBytes = totalBytes;
        this.expiresAt = expiresAt;
    }

    public void complete() {
        this.completed = true;
    }
}
