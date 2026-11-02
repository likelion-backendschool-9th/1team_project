package com.dipsource.entity.user;

import com.dipsource.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 회원. 테이블: users */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(columnDefinition = "text")
    private String bio;

    /** 기술 스택 태그 id 목록. 예: ["spring-boot", "react"] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tech_stacks", columnDefinition = "jsonb")
    private List<String> techStacks = new ArrayList<>();

    @Column(name = "github_username", length = 50)
    private String githubUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserStatus status = UserStatus.ACTIVE;

    /** 칩 잔액. 비정규화 값이라 chip_transactions와 같은 트랜잭션에서 갱신한다. */
    @Column(name = "chip_balance", nullable = false)
    private int chipBalance = 0;

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

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void withdraw() {
        this.status = UserStatus.DELETED;
    }
}
