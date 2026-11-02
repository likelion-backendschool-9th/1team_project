package com.dipsource.entity.post;

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

/** 블로그 글에 붙은 태그. 태그는 tag 기능이라 id만 가진다. 테이블: post_tags */
@Getter
@Entity
@Table(name = "post_tags",
        indexes = @Index(name = "idx_post_tags_tag", columnList = "tag_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTag {

    /** 글 ID·태그 ID 복합 키 */
    @EmbeddedId
    private PostTagId id;

    /** 대상 글 */
    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public PostTag(Post post, Long tagId) {
        this.id = new PostTagId(post.getId(), tagId);
        this.post = post;
    }
}
