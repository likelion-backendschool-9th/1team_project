package com.dipsource.entity.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (demo_id, tag_id) 복합 PK */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoTagId implements Serializable {

    /** 데모 ID */
    @Column(name = "demo_id")
    private Long demoId;

    /** 태그 ID(tags.id) */
    @Column(name = "tag_id")
    private Long tagId;

    public DemoTagId(Long demoId, Long tagId) {
        this.demoId = demoId;
        this.tagId = tagId;
    }
}
