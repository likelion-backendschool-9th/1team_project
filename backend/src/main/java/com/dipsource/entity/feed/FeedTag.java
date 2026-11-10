package com.dipsource.entity.feed;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 피드에 붙은 태그. 태그는 tag 기능이라 id만 가진다. 테이블: feed_tags */
@Getter
@Entity
@Table(name = "feed_tags",
        indexes = @Index(name = "idx_feed_tags_tag", columnList = "tag_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedTag {

    /** 피드 ID·태그 ID 복합 키 */
    @EmbeddedId
    private FeedTagId id;

    /** 대상 피드 */
    @MapsId("feedId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    public FeedTag(Feed feed, Long tagId) {
        this.id = new FeedTagId(feed.getId(), tagId);
        this.feed = feed;
    }
}
