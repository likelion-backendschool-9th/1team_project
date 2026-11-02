package com.dipsource.entity.block;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 차단. 회원은 user 기능이라 id만 가진다. 테이블: blocks */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "blocks",
        indexes = @Index(name = "idx_blocks_blocked", columnList = "blocked_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Block {

    /** 차단한·차단당한 회원 ID 복합 키 */
    @EmbeddedId
    private BlockId id;

    /** 차단한 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Block(Long blockerId, Long blockedId) {
        this.id = new BlockId(blockerId, blockedId);
    }
}
