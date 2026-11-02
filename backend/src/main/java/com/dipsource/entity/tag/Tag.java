package com.dipsource.entity.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 태그. 피드·데모·블로그 글·관심 태그가 함께 쓴다. 테이블: tags */
@Getter
@Entity
@Table(name = "tags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    /** 태그 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 태그 이름. 예: spring, react */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Builder
    private Tag(String name) {
        this.name = name;
    }
}
