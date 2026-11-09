package com.dipsource.entity.search;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 검색 결과에서 들어간 방문 기록. 어떤 검색어로 들어갔는지 함께 남겨서, 다른 검색어에 개인화가 번지지 않게 한다.
 * 조회수 집계와는 별개다. 테이블: search_visits
 */
@Getter
@Entity
@Table(name = "search_visits",
        uniqueConstraints = @UniqueConstraint(name = "uk_search_visit",
                columnNames = {"user_id", "target_type", "target_id", "query_norm"}),
        indexes = @Index(name = "idx_search_visits_last", columnList = "last_visited_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_type", nullable = false, length = 10)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /** 정규화한 검색어 */
    @Column(name = "query_norm", nullable = false, length = 100)
    private String queryNorm;

    /** 의미 있는 방문 횟수. 짧은 시간의 재방문은 세지 않는다 */
    @Column(name = "visit_count", nullable = false)
    private int visitCount;

    @Column(name = "last_visited_at", nullable = false)
    private LocalDateTime lastVisitedAt;
}
