package com.dipsource.service.user.impl;

import com.dipsource.entity.user.SocialAccount;
import com.dipsource.entity.user.SocialProvider;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.UserStatus;
import com.dipsource.global.auth.JwtProvider;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.user.SocialAccountRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.user.AuthResult;
import com.dipsource.service.user.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link AuthService} 구현체. */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public AuthResult findOrCreateFromGithub(
            String providerId,
            String email,
            String githubLogin,
            String avatarUrl
    ) {
        return socialAccountRepository
                .findByProviderAndProviderId(SocialProvider.GITHUB, providerId)
                .map(account -> new AuthResult(account.getUser(), false))
                .orElseGet(() -> createGithubUser(providerId, email, githubLogin, avatarUrl));
    }

    @Override 
    @Transactional(readOnly = true)
    public User findUserByRefreshToken(String refreshToken) {
        if (refreshToken == null || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = jwtProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    private AuthResult createGithubUser(
            String providerId,
            String email,
            String githubLogin,
            String avatarUrl
    ) {
        // 이메일이 비공개면 GitHub noreply 형식으로 채운다.
        String resolvedEmail = (email == null || email.isBlank())
                ? githubLogin + "@users.noreply.github.com"
                : email;

        // 이메일이 이미 있으면 뒤에 숫자를 붙여 UNIQUE를 지킨다.
        resolvedEmail = uniqueEmail(resolvedEmail);

        User user = User.builder()
                .email(resolvedEmail)
                .nickname(uniqueNickname(githubLogin))
                .profileImage(avatarUrl)
                .githubUsername(githubLogin)
                .build();
        userRepository.save(user);

        // users와 social_accounts를 연결한다.
        SocialAccount.builder()
                .user(user)
                .provider(SocialProvider.GITHUB)
                .providerId(providerId)
                .build();

        return new AuthResult(user, true);
    }

    /** 닉네임 중복을 피해 최대 30자 닉네임을 만든다. */
    private String uniqueNickname(String base) {
        String normalized = base == null || base.isBlank() ? "user" : base;
        if (normalized.length() > 30) {
            normalized = normalized.substring(0, 30);
        }
        if (!userRepository.existsByNickname(normalized)) {
            return normalized;
        }
        for (int i = 1; i < 1000; i++) {
            String suffix = String.valueOf(i);
            int maxBase = 30 - suffix.length();
            String candidate = normalized.substring(0, Math.min(normalized.length(), maxBase)) + suffix;
            if (!userRepository.existsByNickname(candidate)) {
                return candidate;
            }
        }
        return normalized.substring(0, Math.min(normalized.length(), 20)) + System.currentTimeMillis();
    }

    private String uniqueEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            return email;
        }
        String local = email;
        String domain = "";
        int at = email.indexOf('@');
        if (at >= 0) {
            local = email.substring(0, at);
            domain = email.substring(at);
        }
        for (int i = 1; i < 1000; i++) {
            String candidate = local + "+" + i + domain;
            if (candidate.length() > 100) {
                candidate = candidate.substring(0, 100);
            }
            if (!userRepository.existsByEmail(candidate)) {
                return candidate;
            }
        }
        return local + "+" + System.currentTimeMillis() + domain;
    }
}
