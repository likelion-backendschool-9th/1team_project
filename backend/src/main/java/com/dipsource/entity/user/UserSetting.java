package com.dipsource.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 회원 설정(알림 받기·공개 범위·DM 허용). 테이블: user_settings
 * 행이 없으면 기본값(전부 켜짐, 전체 공개)으로 본다. 처음 저장할 때 행을 만든다.
 */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_settings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSetting {

    public static final String VISIBILITY_PUBLIC = "public";

    /** 회원 ID (users.id와 1:1) */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /** 좋아요 알림 */
    @Column(name = "notify_like", nullable = false)
    private boolean notifyLike = true;

    /** 댓글 알림 */
    @Column(name = "notify_comment", nullable = false)
    private boolean notifyComment = true;

    /** 팔로우 알림 */
    @Column(name = "notify_follow", nullable = false)
    private boolean notifyFollow = true;

    /** DM 알림 */
    @Column(name = "notify_dm", nullable = false)
    private boolean notifyDm = true;

    /** 방명록 알림 */
    @Column(name = "notify_guestbook", nullable = false)
    private boolean notifyGuestbook = true;

    /** 프로필 공개 범위: public / followers / private */
    @Column(nullable = false, length = 20)
    private String visibility = VISIBILITY_PUBLIC;

    /** 팔로우하지 않은 사람의 DM 허용 */
    @Column(name = "allow_dm", nullable = false)
    private boolean allowDm = true;

    /** 마지막으로 바꾼 시각 */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 기본값 설정 (아직 저장한 적 없는 회원) */
    public static UserSetting defaults(Long userId) {
        UserSetting setting = new UserSetting();
        setting.userId = userId;
        return setting;
    }

    public void update(boolean notifyLike, boolean notifyComment, boolean notifyFollow, boolean notifyDm,
                       boolean notifyGuestbook, String visibility, boolean allowDm) {
        this.notifyLike = notifyLike;
        this.notifyComment = notifyComment;
        this.notifyFollow = notifyFollow;
        this.notifyDm = notifyDm;
        this.notifyGuestbook = notifyGuestbook;
        this.visibility = visibility;
        this.allowDm = allowDm;
    }
}
