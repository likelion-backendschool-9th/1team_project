package com.dipsource.entity.post;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (user_id, post_id) 복합 PK. post_likes, post_bookmarks, post_purchases가 함께 쓴다. */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostUserId implements Serializable {

    /** 회원 ID(users.id) */
    @Column(name = "user_id")
    private Long userId;

    /** 글 ID */
    @Column(name = "post_id")
    private Long postId;

    public PostUserId(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }
}
