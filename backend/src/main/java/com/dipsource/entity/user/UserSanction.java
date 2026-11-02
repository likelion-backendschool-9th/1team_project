package com.dipsource.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 회원 제재 이력. 테이블: user_sanctions */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_sanctions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSanction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 제재 대상 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 제재한 관리자(admins.id). admin 엔티티를 직접 참조하지 않고 id만 가진다. */
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(length = 500)
    private String reason;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    /** 제재 종료 예정 시각 */
    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    /** 기간 전에 관리자가 해제한 시각. 해제하지 않았으면 null. */
    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private UserSanction(User user, Long adminId, String reason,
                         LocalDateTime startsAt, LocalDateTime endsAt) {
        this.user = user;
        this.adminId = adminId;
        this.reason = reason;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public void release() {
        this.releasedAt = LocalDateTime.now();
    }
}
