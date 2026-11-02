package com.dipsource.entity.feed;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (user_id, feed_id) 복합 PK. feed_likes, feed_reposts, feed_bookmarks가 함께 쓴다. */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedUserId implements Serializable {

    /** 회원 ID(users.id) */
    @Column(name = "user_id")
    private Long userId;

    /** 피드 ID */
    @Column(name = "feed_id")
    private Long feedId;

    public FeedUserId(Long userId, Long feedId) {
        this.userId = userId;
        this.feedId = feedId;
    }
}
