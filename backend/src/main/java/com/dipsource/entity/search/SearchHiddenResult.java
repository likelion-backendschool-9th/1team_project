package com.dipsource.entity.search;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 숨긴 검색 결과. 그 사용자의 검색에만 적용한다. 테이블: search_hidden_results
 * 운영자 제재는 콘텐츠 상태(HIDDEN)로 처리하므로 이 테이블과 섞이지 않는다.
 */
@Getter
@Entity
@Table(name = "search_hidden_results", uniqueConstraints = @UniqueConstraint(
        name = "uk_search_hidden_user_target", columnNames = {"user_id", "target_type", "target_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchHiddenResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** POST, DEMO, FEED, USER */
    @Column(name = "target_type", nullable = false, length = 10)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
