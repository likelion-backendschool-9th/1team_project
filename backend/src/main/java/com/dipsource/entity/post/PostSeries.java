package com.dipsource.entity.post;

import com.dipsource.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 블로그 연재 시리즈. 현금으로 한 번 사면 이후 회차까지 계속 읽는다 (인프런 방식). 테이블: post_series
 * 회차는 posts.series_id·series_order로 붙는다.
 */
@Getter
@Entity
@Table(name = "post_series", indexes = @Index(name = "idx_post_series_user", columnList = "user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostSeries extends BaseEntity {

    /** 시리즈 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작가 ID(users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 제목 */
    @Column(nullable = false, length = 80)
    private String title;

    /** 소개 */
    @Column(nullable = false, length = 500)
    private String description;

    /** 판매 가격(원). 0이면 무료 시리즈라 유료 회차를 둘 수 없다. 상한·하한 없음 (팀 결정) */
    @Column(name = "price_krw", nullable = false)
    private int priceKrw;

    /** 연재 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeriesStatus status = SeriesStatus.ONGOING;

    @Builder
    private PostSeries(Long userId, String title, String description, int priceKrw) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.priceKrw = priceKrw;
    }

    public void update(String title, String description, int priceKrw, SeriesStatus status) {
        this.title = title;
        this.description = description;
        this.priceKrw = priceKrw;
        this.status = status;
    }
}
