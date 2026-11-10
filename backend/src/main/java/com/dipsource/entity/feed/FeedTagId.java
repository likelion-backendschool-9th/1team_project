package com.dipsource.entity.feed;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (feed_id, tag_id) 복합 PK */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedTagId implements Serializable {

    /** 피드 ID */
    @Column(name = "feed_id")
    private Long feedId;

    /** 태그 ID(tags.id) */
    @Column(name = "tag_id")
    private Long tagId;

    public FeedTagId(Long feedId, Long tagId) {
        this.feedId = feedId;
        this.tagId = tagId;
    }
}
