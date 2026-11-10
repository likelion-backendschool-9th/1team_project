package com.dipsource.entity.search;

import com.dipsource.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 통합 검색 기록. 인기 검색어 집계에 쓴다. 테이블: search_keyword_logs */
@Getter
@Entity
@Table(name = "search_keyword_logs", indexes = {
        @Index(name = "idx_search_keyword_logs_user", columnList = "user_id, keyword, searched_at"),
        @Index(name = "idx_search_keyword_logs_searched", columnList = "searched_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchKeywordLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 정규화한 검색어 */
    @Column(nullable = false, length = 100)
    private String keyword;

    /** 로그인한 사용자. 비로그인이면 null */
    @Column(name = "user_id")
    private Long userId;

    /** 검색 시각 (Asia/Seoul) */
    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    public static SearchKeywordLog create(String keyword, Long userId, LocalDateTime searchedAt) {
        SearchKeywordLog log = new SearchKeywordLog();
        log.keyword = keyword;
        log.userId = userId;
        log.searchedAt = searchedAt;
        return log;
    }
}
