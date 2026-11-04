package com.dipsource.service.user;

import com.dipsource.entity.user.User;

/** 소셜 로그인 회원을 찾거나 새로 만드는 서비스. */
public interface AuthService {

    User findUserByRefreshToken(String refreshToken);

    /** GitHub 프로필로 기존 회원을 찾거나 신규 가입시킨다. */
    AuthResult findOrCreateFromGithub(
            String providerId,
            String email,
            String githubLogin,
            String avatarUrl
    );
}
