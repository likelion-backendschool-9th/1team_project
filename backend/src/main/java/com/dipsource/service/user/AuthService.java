package com.dipsource.service.user;

import java.util.List;

import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.SocialProvider;

/** 소셜 로그인 회원을 찾거나 새로 만드는 서비스. */
public interface AuthService {

    User findUserByRefreshToken(String refreshToken);

    /** 프로필로 기존 회원을 찾거나 신규 가입시킨다. */
    AuthResult findOrCreate(SocialProfile profile);

    IssuedTokens issueTokens(User user);          // 로그인. 새 family
    
    IssuedTokens rotate(String refreshToken);     // refresh

    void revokePresented(String refreshToken);    // logout. 이 기기 줄만

    void revokeAll(Long userId);                  // 탈퇴

    void linkSocialAccount(Long userId, SocialProfile profile);              // 내 정보에서 소셜 계정 추가 연결

    List<SocialProvider> getLinkedProviders(Long userId);                    // 연결된 소셜 목록

    void unlinkSocialAccount(Long userId, SocialProvider provider);         // 연결 해제 (하나는 남겨야 함)

    record IssuedTokens(String accessToken, String refreshToken) {}
    
    
}
