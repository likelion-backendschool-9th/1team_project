package com.dipsource.entity.demo;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 데모 북마크(나중에 해보기). 테이블: bookmarks */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "bookmarks",
        indexes = @Index(name = "idx_bookmarks_demo", columnList = "demo_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoBookmark {

    /** 회원 ID·데모 ID 복합 키 */
    @EmbeddedId
    private DemoUserId id;

    /** 대상 데모 */
    @MapsId("demoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demo_id", nullable = false)
    private Demo demo;

    /** 담은 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DemoBookmark(Long userId, Demo demo) {
        this.id = new DemoUserId(userId, demo.getId());
        this.demo = demo;
    }
}
