package com.dipsource.entity.user.enums;

public enum UserRole {
    USER,
    ADMIN,
    /** 최고 관리자. ADMIN 권한 전부 + 다른 회원에게 ADMIN을 주거나 USER로 내리는 권한 */
    MASTER;

    /** 관리자 화면·API를 쓸 수 있는 권한인지 (ADMIN, MASTER) */
    public boolean isAdmin() {
        return this == ADMIN || this == MASTER;
    }
}
