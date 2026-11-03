package com.dipsource.entity.badge;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 뱃지 종류. 테이블: badges */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "badges")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Badge {

    /** 뱃지 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 뱃지를 구분하는 코드 */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** 뱃지 이름. 예: 고인물 */
    @Column(nullable = false, length = 30)
    private String name;

    /** 아이콘(이모지 또는 이미지 URL) */
    @Column(length = 100)
    private String icon;

    /** 표시 색상 */
    @Column(length = 20)
    private String color;

    /** 설명 */
    @Column(length = 200)
    private String description;

    /** 획득 조건 유형 */
    @Column(name = "condition_type", nullable = false, length = 30)
    private String conditionType;

    /** 획득 조건 값 */
    @Column(name = "condition_value", length = 100)
    private String conditionValue;

    /** 사용 여부. false면 새로 지급하지 않는다. */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** 뱃지 등록 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Badge(String code, String name, String icon, String color, String description,
                  String conditionType, String conditionValue) {
        this.code = code;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.description = description;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
    }

    public void deactivate() {
        this.active = false;
    }
}
