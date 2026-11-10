package com.dipsource.controller.admin;

import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;

/** 관리자 컨트롤러에서 로그인 회원 ID를 꺼낸다. */
final class AdminAuth {

    private AdminAuth() {
    }

    static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
