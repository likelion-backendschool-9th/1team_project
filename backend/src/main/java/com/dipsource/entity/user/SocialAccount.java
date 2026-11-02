package com.dipsource.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 소셜 로그인 연동 정보. 테이블: social_accounts */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "social_accounts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialProvider provider;

    /** 소셜 측 고유 ID */
    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private SocialAccount(User user, SocialProvider provider, String providerId) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
    }
}
