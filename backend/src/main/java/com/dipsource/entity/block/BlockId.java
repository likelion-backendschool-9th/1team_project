package com.dipsource.entity.block;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (blocker_id, blocked_id) 복합 PK */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockId implements Serializable {

    /** 차단한 회원 ID(users.id) */
    @Column(name = "blocker_id")
    private Long blockerId;

    /** 차단당한 회원 ID(users.id) */
    @Column(name = "blocked_id")
    private Long blockedId;

    public BlockId(Long blockerId, Long blockedId) {
        this.blockerId = blockerId;
        this.blockedId = blockedId;
    }
}
