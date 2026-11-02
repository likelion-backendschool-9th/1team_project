package com.dipsource.entity.minihome;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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

/** 미니홈피 방명록. 테이블: guestbooks */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "guestbooks",
        indexes = @Index(name = "idx_guestbooks_home_created", columnList = "home_id, created_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Guestbook {

    /** 방명록 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 방명록이 달린 미니홈피 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_id", nullable = false)
    private MiniHome home;

    /** 작성자 ID(users.id) */
    @Column(name = "writer_id", nullable = false)
    private Long writerId;

    /** 내용 */
    @Column(nullable = false, length = 500)
    private String content;

    /** 비밀글 여부. 주인과 작성자만 볼 수 있다 */
    @Column(name = "is_secret", nullable = false)
    private boolean secret = false;

    /** 작성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 삭제 시각. 삭제하지 않았으면 null */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Guestbook(MiniHome home, Long writerId, String content, boolean secret) {
        this.home = home;
        this.writerId = writerId;
        this.content = content;
        this.secret = secret;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
