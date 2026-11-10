package com.dipsource.entity.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (user_id, demo_id) 복합 PK. demo_likes, bookmarks가 함께 쓴다. */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoUserId implements Serializable {

    /** 회원 ID(users.id) */
    @Column(name = "user_id")
    private Long userId;

    /** 데모 ID */
    @Column(name = "demo_id")
    private Long demoId;

    public DemoUserId(Long userId, Long demoId) {
        this.userId = userId;
        this.demoId = demoId;
    }
}
