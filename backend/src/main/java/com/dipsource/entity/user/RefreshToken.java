package com.dipsource.entity.user;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import lombok.AccessLevel;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "refresh_tokens", uniqueConstraints = {
                @UniqueConstraint(columnNames = "jti"),
                @UniqueConstraint(columnNames = "token_hash")
}, indexes = {
                @Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
                @Index(name = "idx_refresh_tokens_family", columnList = "family_id"),
                @Index(name = "idx_refresh_tokens_expires", columnList = "expires_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "user_id", nullable = false)
        private Long userId; // User FK 아님. id만

        @Column(nullable = false, unique = true, length = 36)
        private String jti;

        @Column(name = "token_hash", nullable = false, unique = true, length = 64)
        private String tokenHash;

        @Column(name = "family_id", nullable = false, length = 36)
        private String familyId;

        @Column(name = "issued_at", nullable = false)
        private LocalDateTime issuedAt;

        @Column(name = "expires_at", nullable = false)
        private LocalDateTime expiresAt;

        @Column(name = "rotated_at")
        private LocalDateTime rotatedAt; // null = 아직 현역

        @Column(name = "revoked_at")
        private LocalDateTime revokedAt;

        @Column(name = "replaced_by_jti", length = 36)
        private String replacedByJti; // 유예 때 다음 토큰을 찾기 위함

        @CreatedDate
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Builder
        private RefreshToken(Long userId, String jti, String tokenHash, String familyId,
                        LocalDateTime issuedAt, LocalDateTime expiresAt) {
                this.userId = userId;
                this.jti = jti;
                this.tokenHash = tokenHash;
                this.familyId = familyId;
                this.issuedAt = issuedAt;
                this.expiresAt = expiresAt;
        }

        public void markRotated(LocalDateTime now, String nextJti) {
                this.rotatedAt = now;
                this.replacedByJti = nextJti;
        }

        public void revoke(LocalDateTime now) {
                this.revokedAt = now;
        }

}
