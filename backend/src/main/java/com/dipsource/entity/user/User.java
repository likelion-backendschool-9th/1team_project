package com.dipsource.entity.user;

import com.dipsource.global.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 회원. 테이블: users
 * 탈퇴해도 행은 지우지 않는다(작성한 글·댓글이 남아야 하므로). 대신 개인정보를 비우고 소셜 연결을 끊는다.
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    /** 탈퇴한 회원을 화면에 보여 줄 때 쓰는 이름 */
    public static final String WITHDRAWN_NICKNAME = "삭제된 유저";

    /** 회원 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 이메일. 탈퇴하면 deleted_{id}@deleted.dipsource로 바뀐다 */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** 닉네임. 탈퇴하면 deleted_{id}로 바뀐다 */
    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    /** 프로필 이미지 URL */
    @Column(name = "profile_image", length = 500)
    private String profileImage;

    /** 자기소개 */
    @Column(length = 200)
    private String bio;

    /** 기술 스택 태그 id 목록. 예: ["spring-boot", "react"] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tech_stacks", columnDefinition = "jsonb")
    private List<String> techStacks = new ArrayList<>();

    /** 깃허브 아이디 */
    @Column(name = "github_username", length = 50)
    private String githubUsername;

    /** 권한. 관리자는 admins 테이블에 따로 있어 회원은 USER만 쓴다 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    /** 회원 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    /** 칩 잔액. 비정규화 값이라 chip_transactions와 같은 트랜잭션에서 갱신한다. */
    @Column(name = "chip_balance", nullable = false)
    private int chipBalance = 0;

    /** 마지막 활동 시각. DM에서 "활동중"을 판단하는 기준이다. */
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    /** 탈퇴 시각. 탈퇴하지 않았으면 null. */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** 연결된 소셜 계정. 탈퇴할 때 함께 지운다 */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @Builder
    private User(String email, String nickname, String profileImage, String bio,
                 List<String> techStacks, String githubUsername) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.bio = bio;
        this.techStacks = techStacks == null ? new ArrayList<>() : new ArrayList<>(techStacks);
        this.githubUsername = githubUsername;
    }

    public void updateProfile(String nickname, String profileImage, String bio,
                              List<String> techStacks, String githubUsername) {
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.bio = bio;
        this.techStacks = techStacks == null ? new ArrayList<>() : new ArrayList<>(techStacks);
        this.githubUsername = githubUsername;
    }

    /** 활동이 있을 때 호출해 마지막 활동 시각을 갱신한다. */
    public void markActive() {
        this.lastActiveAt = LocalDateTime.now();
    }

    /** 마지막 활동이 기준 시간 안이면 활동중이다. 활동 기록이 없으면 false. */
    public boolean isActiveWithin(Duration threshold) {
        return lastActiveAt != null
                && lastActiveAt.isAfter(LocalDateTime.now().minus(threshold));
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public boolean isWithdrawn() {
        return status == UserStatus.DELETED;
    }

    /** 화면에 보여 줄 닉네임. 탈퇴한 회원이면 "삭제된 유저"다. */
    public String getDisplayNickname() {
        return isWithdrawn() ? WITHDRAWN_NICKNAME : nickname;
    }

    /**
     * 탈퇴. 작성한 글·댓글은 남기고, 개인정보는 비워 누구인지 알 수 없게 한다.
     * 이메일·닉네임은 UNIQUE라 비울 수 없어 회원 ID로 만든 값으로 바꾼다. 그래서 같은 이메일·닉네임으로 다시 가입할 수 있다.
     * 소셜 계정 연결도 지워서, 같은 소셜 계정으로 로그인하면 새 회원으로 가입된다.
     */
    public void withdraw() {
        this.status = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.email = "deleted_" + id + "@deleted.dipsource";
        this.nickname = "deleted_" + id;
        this.profileImage = null;
        this.bio = null;
        this.techStacks = new ArrayList<>();
        this.githubUsername = null;
        this.lastActiveAt = null;
        this.socialAccounts.clear();
    }
}
