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

/** 미니홈피 일촌평. 테이블: mini_home_comments */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "mini_home_comments",
        indexes = @Index(name = "idx_mhc_home_created", columnList = "home_id, created_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MiniHomeComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 일촌평이 달린 미니홈피 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_id", nullable = false)
    private MiniHome home;

    /** 작성자 ID(users.id) */
    @Column(name = "writer_id", nullable = false)
    private Long writerId;

    /** 내용 (100자) */
    @Column(nullable = false, length = 100)
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 삭제 시각. null이면 삭제되지 않음 */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private MiniHomeComment(MiniHome home, Long writerId, String content) {
        this.home = home;
        this.writerId = writerId;
        this.content = content;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
