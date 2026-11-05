package com.dipsource.entity.minihome;

import com.dipsource.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 미니홈피 일촌평. 한 미니홈피에 작성자당 하나. 테이블: mini_home_comments */
@Getter
@Entity
@Table(name = "mini_home_comments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_mini_home_comments_home_writer",
                columnNames = {"home_id", "writer_id"}),
        indexes = @Index(name = "idx_mini_home_comments_home_updated", columnList = "home_id, updated_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MiniHomeComment extends BaseEntity {

    /** 일촌평 ID */
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

    /** 내용 */
    @Column(nullable = false, length = 100)
    private String content;

    /** 삭제 시각. 삭제하지 않았으면 null */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private MiniHomeComment(MiniHome home, Long writerId, String content) {
        this.home = home;
        this.writerId = writerId;
        this.content = content;
    }

    /** 내용을 바꾸고, 지워진 글이면 다시 살린다. */
    public void rewrite(String content) {
        this.content = content;
        this.deletedAt = null;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
