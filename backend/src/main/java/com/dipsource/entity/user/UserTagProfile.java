package com.dipsource.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 회원의 활동으로 쌓이는 태그별 가중치(추천용). 테이블: user_tag_profile */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_tag_profile")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTagProfile {

    /** 회원 ID·태그 ID 복합 키 */
    @EmbeddedId
    private UserTagId id;

    /** 대상 회원 */
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 가중치. 활동할수록 커진다 */
    @Column(nullable = false)
    private int weight;

    /** 가중치 갱신 시각 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserTagProfile(User user, Long tagId, int weight) {
        this.id = new UserTagId(user.getId(), tagId);
        this.user = user;
        this.weight = weight;
    }

    public void addWeight(int delta) {
        this.weight += delta;
    }
}
