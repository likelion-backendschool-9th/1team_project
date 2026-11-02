package com.dipsource.service.user;

import com.dipsource.entity.user.User;

/**
 * 소셜 로그인 처리 결과.
 * isNew가 true면 온보딩으로 보낸다.
 */
public record AuthResult(User user, boolean isNew) {
}
