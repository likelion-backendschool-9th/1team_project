package com.dipsource.entity.demo;

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

/** 데모에 붙은 기술 태그. 태그는 tag 기능이라 id만 가진다. 테이블: demo_tags */
@Getter
@Entity
@Table(name = "demo_tags",
        indexes = @Index(name = "idx_demo_tags_tag", columnList = "tag_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoTag {

    /** 데모 ID·태그 ID 복합 키 */
    @EmbeddedId
    private DemoTagId id;

    /** 대상 데모 */
    @MapsId("demoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demo_id", nullable = false)
    private Demo demo;

    public DemoTag(Demo demo, Long tagId) {
        this.id = new DemoTagId(demo.getId(), tagId);
        this.demo = demo;
    }
}
