package com.dipsource.entity.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/** 관리자 계정. 회원(users)과 별개로 아이디·비밀번호로 로그인한다. 테이블: admins */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "admins")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {

    /** 관리자 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 로그인 아이디 */
    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    /** 비밀번호. 암호화(BCrypt)한 값만 저장한다 */
    @Column(nullable = false, length = 255)
    private String password;

    /** 이름 */
    @Column(nullable = false, length = 30)
    private String name;

    /** 권한 */
    @Enumerated(EnumType.STRING)
    @Column(name = "admin_role", nullable = false, length = 20)
    private AdminRole adminRole = AdminRole.STAFF;

    /** 마지막 로그인 시각 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /** 계정 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 삭제 시각. 삭제하지 않았으면 null */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Admin(String loginId, String password, String name, AdminRole adminRole) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.adminRole = adminRole == null ? AdminRole.STAFF : adminRole;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
