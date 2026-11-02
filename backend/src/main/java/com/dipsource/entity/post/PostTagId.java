package com.dipsource.entity.post;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (post_id, tag_id) 복합 PK */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTagId implements Serializable {

    /** 글 ID */
    @Column(name = "post_id")
    private Long postId;

    /** 태그 ID(tags.id) */
    @Column(name = "tag_id")
    private Long tagId;

    public PostTagId(Long postId, Long tagId) {
        this.postId = postId;
        this.tagId = tagId;
    }
}
