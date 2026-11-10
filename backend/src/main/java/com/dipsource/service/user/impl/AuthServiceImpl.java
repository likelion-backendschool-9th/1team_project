package com.dipsource.service.user.impl;

import com.dipsource.entity.user.SocialAccount;
import com.dipsource.service.user.SocialProfile;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.SocialProvider;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.auth.JwtProvider;
import com.dipsource.global.auth.RefreshTokenHasher;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.user.RefreshTokenRepository;
import com.dipsource.repository.user.SocialAccountRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.user.AuthResult;
import com.dipsource.service.user.AuthService;
import com.dipsource.entity.user.RefreshToken;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link AuthService} 구현체. */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Duration ROTATION_GRACE = Duration.ofSeconds(30);

    private static LocalDateTime toSeoul(Instant instant) {
        return LocalDateTime.ofInstant(instant, SEOUL);
    }

    /** 재사용 감지 후 401을 던져도 family 폐기는 커밋되어야 한다. */
    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public IssuedTokens rotate(String refreshToken) {
        if (refreshToken == null || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String tokenHash = RefreshTokenHasher.sha256(refreshToken);
        // 잠금 전에 엔티티를 읽으면 잠금 뒤 재조회도 1차 캐시의 낡은 값(폐기 전 상태)을 돌려준다.
        // 그러면 다른 트랜잭션이 family를 폐기한 직후에도 현역으로 보고 새 토큰을 발급해 버린다.
        // 그래서 잠금 전에는 family ID만 읽고, 엔티티는 잠금을 잡은 뒤 처음 읽는다.
        String familyId = refreshTokenRepository.findFamilyIdByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        refreshTokenRepository.lockFamily(familyId);
        RefreshToken current = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        Instant now = Instant.now();
        LocalDateTime nowSeoul = toSeoul(now);

        if (current.getRevokedAt() != null || current.getExpiresAt().isBefore(nowSeoul)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(current.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        //현역: 새 토큰 발급하고 옛 토큰에 rotated_at + 다음 jti 기록
        if (current.getRotatedAt() == null) {
            JwtProvider.IssuedRefreshToken next =
                    jwtProvider.createRefreshToken(user.getId(), UUID.fromString(current.getFamilyId()));
            current.markRotated(nowSeoul, next.jti());
            refreshTokenRepository.save(RefreshToken.builder()
                    .userId(user.getId())
                    .jti(next.jti())
                    .tokenHash(RefreshTokenHasher.sha256(next.token()))
                    .familyId(current.getFamilyId())
                    .issuedAt(toSeoul(next.issuedAt()))
                    .expiresAt(toSeoul(next.expiresAt()))
                    .build());
            String access = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
            return new IssuedTokens(access, next.token());
        }

        // 교체 후 30초 안: 체인 끝(아직 교체되지 않은 토큰)을 돌려준다. A→B→C면 A 재요청은 C.
        Duration age = Duration.between(
                current.getRotatedAt().atZone(SEOUL).toInstant(), now);
        if (age.compareTo(ROTATION_GRACE) <= 0) {
            RefreshToken latest = latestUnrotated(current);
            String same = jwtProvider.recreateRefreshToken(
                    latest.getUserId(),
                    latest.getJti(),
                    latest.getIssuedAt().atZone(SEOUL).toInstant(),
                    latest.getExpiresAt().atZone(SEOUL).toInstant());
            String access = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
            return new IssuedTokens(access, same);
        }

        refreshTokenRepository.revokeFamily(current.getFamilyId(), nowSeoul);
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    /** A→B→C처럼 이어진 교체에서, 아직 현역인 마지막 토큰을 찾는다. */
    private RefreshToken latestUnrotated(RefreshToken start) {
        RefreshToken cursor = start;
        for (int hops = 0; hops < 32 && cursor.getRotatedAt() != null; hops++) {
            if (cursor.getReplacedByJti() == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            cursor = refreshTokenRepository.findByJti(cursor.getReplacedByJti())
                    .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
            if (cursor.getRevokedAt() != null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
        }
        if (cursor.getRotatedAt() != null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return cursor;
    }

    @Override
    @Transactional
    public void revokePresented(String refreshToken) {
        if (refreshToken == null || !jwtProvider.isRefreshToken(refreshToken)) {
            return;
        }
        refreshTokenRepository.findByTokenHash(RefreshTokenHasher.sha256(refreshToken))
                .ifPresent(token -> {
                    refreshTokenRepository.lockFamily(token.getFamilyId());
                    refreshTokenRepository.revokeFamily(token.getFamilyId(), toSeoul(Instant.now()));
                });
    }

    @Override
    @Transactional
    public void revokeAll(Long userId) {
        refreshTokenRepository.findDistinctFamilyIdsByUserId(userId).stream()
                .sorted()
                .forEach(refreshTokenRepository::lockFamily);
        refreshTokenRepository.revokeAllByUserId(userId, toSeoul(Instant.now()));
    }

    @Override
    @Transactional
    public AuthResult findOrCreate(SocialProfile profile) {
        return socialAccountRepository
                .findByProviderAndProviderId(profile.provider(), profile.providerId())
                .map(account -> {
                    User user = account.getUser();
                    user.syncGithubUsername(profile.githubUsername()); // GitHub 아이디가 바뀌었으면 따라간다
                    return new AuthResult(user, false);
                })
                .orElseGet(() -> createUser(profile));
    }

    @Override
    @Transactional
    public void linkSocialAccount(Long userId, SocialProfile profile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        // 이 소셜 계정이 이미 어딘가에 연결돼 있는지 먼저 본다
        Optional<SocialAccount> existing =
                socialAccountRepository.findByProviderAndProviderId(profile.provider(), profile.providerId());
        if (existing.isPresent()) {
            if (existing.get().getUser().getId().equals(userId)) {
                return; // 이미 내 계정에 연결돼 있으면 그대로 성공
            }
            throw new BusinessException(ErrorCode.CONFLICT, "이미 다른 찍먹 계정에 연결된 소셜 계정이에요.");
        }

        // 같은 종류(예: 카카오)를 다른 카카오 계정으로 하나 더 붙이는 건 막는다
        boolean sameProviderLinked = user.getSocialAccounts().stream()
                .anyMatch(account -> account.getProvider() == profile.provider());
        if (sameProviderLinked) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 같은 종류의 소셜 계정이 연결돼 있어요.");
        }

        // builder 안에서 user.getSocialAccounts()에 추가되고, cascade로 함께 저장된다
        SocialAccount.builder()
                .user(user)
                .provider(profile.provider())
                .providerId(profile.providerId())
                .build();
        user.syncGithubUsername(profile.githubUsername()); // GitHub을 연결하면 잔디용 아이디도 채운다
    }

    @Override
    @Transactional(readOnly = true)
    public List<SocialProvider> getLinkedProviders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return user.getSocialAccounts().stream()
                .map(SocialAccount::getProvider)
                .sorted()
                .toList();
    }

    @Override
    @Transactional
    public void unlinkSocialAccount(Long userId, SocialProvider provider) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        // 소셜 로그인만 쓰므로, 다 끊으면 다시 로그인할 방법이 없어진다
        if (user.getSocialAccounts().size() <= 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "로그인할 소셜 계정이 하나는 남아 있어야 해요.");
        }
        boolean removed = user.getSocialAccounts().removeIf(account -> account.getProvider() == provider);
        if (!removed) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "연결되지 않은 소셜 계정이에요.");
        }
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

    @Override
    @Transactional
    public IssuedTokens issueTokens(User user) {
        JwtProvider.IssuedRefreshToken issued =
                jwtProvider.createRefreshToken(user.getId(), UUID.randomUUID());
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .jti(issued.jti())
                .tokenHash(RefreshTokenHasher.sha256(issued.token()))
                .familyId(issued.familyId().toString())
                .issuedAt(toSeoul(issued.issuedAt()))
                .expiresAt(toSeoul(issued.expiresAt()))
                .build());
        String access = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        return new IssuedTokens(access, issued.token());
    }

    private AuthResult createUser(SocialProfile profile) {
         // 이메일이 비공개면 GitHub noreply 형식으로 채운다. (구글은 항상 이메일이 온다)
        String email = profile.email();
        String resolvedEmail = (email == null || email.isBlank())
                ? fallbackEmail(profile)
                : email;

        // 이메일이 이미 있으면 뒤에 숫자를 붙여 UNIQUE를 지킨다.
        resolvedEmail = uniqueEmail(resolvedEmail);

        User user = User.builder()
                .email(resolvedEmail)
                .nickname(uniqueNickname(profile.nickname()))
                .profileImage(profile.profileImage())
                .githubUsername(profile.githubUsername())
                .build();
        userRepository.save(user);

        // users와 social_accounts를 연결한다.
        SocialAccount.builder()
                .user(user)
                .provider(profile.provider())
                .providerId(profile.providerId())
                .build();

                // 지금 바로 DB에 반영해서, 중복 가입이면 여기서 DataIntegrityViolationException이 나게 한다.
        userRepository.flush();

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

    private static String fallbackEmail(SocialProfile profile) {
        return switch (profile.provider()) {
            case GITHUB -> profile.nickname() + "@users.noreply.github.com";
            case KAKAO -> "kakao_" + profile.providerId() + "@noreply.dipsource";
            case GOOGLE -> profile.providerId() + "@noreply.dipsource";
        };
    }


    

}
