package com.dipsource.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (user_id, tag_id) 복합 PK. user_interest_tags, user_tag_profile이 함께 쓴다. */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTagId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    /** tags.id. tag 도메인 엔티티를 직접 참조하지 않고 id만 가진다. */
    @Column(name = "tag_id")
    private Long tagId;

    public UserTagId(Long userId, Long tagId) {
        this.userId = userId;
        this.tagId = tagId;
    }
}
