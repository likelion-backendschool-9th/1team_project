package com.dipsource.service.user.impl;

import com.dipsource.dto.user.CompleteProfileRequest;
import com.dipsource.dto.user.NicknameAvailableResponse;
import com.dipsource.dto.user.UserMeResponse;
import com.dipsource.dto.user.UserProfileResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.tag.Tag;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.UserInterestTag;
import com.dipsource.entity.user.UserStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.repository.tag.TagRepository;
import com.dipsource.repository.user.UserInterestTagRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.user.UserService;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link UserService} 구현체. */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9_]+$");
    private static final Pattern GITHUB_PATTERN = Pattern.compile("^[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,38})$");
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    /** 마지막 활동이 이 시간 안이면 "접속 중"으로 본다. */
    private static final Duration ONLINE_THRESHOLD = Duration.ofMinutes(5);

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final UserInterestTagRepository userInterestTagRepository;
    private final FollowRepository followRepository;



    @Override
    @Transactional(readOnly = true)
    public UserMeResponse getMe(Long userId) {
        return UserMeResponse.from(getUser(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public NicknameAvailableResponse isNicknameAvailable(Long userId, String nickname) {
        String trimmed = nickname == null ? "" : nickname.trim();
        if (trimmed.isEmpty() || !NICKNAME_PATTERN.matcher(trimmed).matches()
                || trimmed.length() < 2 || trimmed.length() > 30) {
            return new NicknameAvailableResponse(false);
        }
        User me = getUser(userId);
        if (trimmed.equals(me.getNickname())) {
            return new NicknameAvailableResponse(true);
        }
        return new NicknameAvailableResponse(!userRepository.existsByNickname(trimmed));
    }

    @Override
    @Transactional
    public UserMeResponse completeProfile(Long userId, CompleteProfileRequest request) {
        User user = getUser(userId);

        String nickname = request.nickname().trim();
        validateNickname(nickname);
        if (!nickname.equals(user.getNickname()) && userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_TAKEN);
        }

        String bio = request.bio() == null ? null : request.bio().trim();
        if (bio != null && bio.isEmpty()) {
            bio = null;
        }

        String githubUsername = request.githubUsername() == null ? null : request.githubUsername().trim();
        if (githubUsername != null && githubUsername.isEmpty()) {
            githubUsername = null;
        }
        if (githubUsername != null && !GITHUB_PATTERN.matcher(githubUsername).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "올바른 GitHub 사용자명이 아니에요.");
        }

        List<String> techStacks = normalizeSlugs(request.techStacks(), "기술 스택");
        List<String> interestTags = normalizeSlugs(request.interestTags(), "관심 태그");
        if (techStacks.size() < 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "기술 스택을 2개 이상 골라 주세요.");
        }
        if (interestTags.size() < 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "관심 태그를 2개 이상 골라 주세요.");
        }

        // 프로필 이미지·이메일은 유지하고 온보딩에서 받은 값만 갱신한다.
        user.updateProfile(nickname, user.getProfileImage(), bio, techStacks, githubUsername);

        // 관심 태그는 통째로 교체한다.
        userInterestTagRepository.deleteAllByUserId(user.getId());
        for (String slug : interestTags) {
            Tag tag = tagRepository.findByName(slug)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(slug).build()));
            userInterestTagRepository.save(new UserInterestTag(user, tag.getId()));
        }

        return UserMeResponse.from(user);
    }

    @Override 
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId, Long viewerId) {
        User user = getUser(userId);
        if (user.isWithdrawn()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다.");
        }
        boolean following = viewerId != null
                && !viewerId.equals(userId)
                && followRepository.existsByIdFollowerIdAndIdFollowingId(viewerId, userId);
        return  UserProfileResponse.of(user, following);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다"));
    }

    private void validateNickname(String nickname) {
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "한글, 영문, 숫자, 밑줄(_)만 쓸 수 있어요.");
        }
    }

    private List<String> normalizeSlugs(List<String> raw, String label) {
        if (raw == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, label + "을(를) 골라 주세요.");
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String item : raw) {
            if (item == null || item.isBlank()) {
                continue;
            }
            String slug = item.trim();
            if (!SLUG_PATTERN.matcher(slug).matches()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "목록에 없는 " + label + "이(가) 있어요.");
            }
            unique.add(slug);
        }
        return List.copyOf(unique);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActiveUser(Long userId) {
        return userRepository.existsByIdAndStatus(userId, UserStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, UserSummaryResponse> getSummaries(Collection<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, UserSummaryResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getOnlineUserIds(Collection<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .filter(user -> !user.isWithdrawn() && user.isActiveWithin(ONLINE_THRESHOLD))
                .map(User::getId)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void markActive(Long userId) {
        userRepository.findById(userId).ifPresent(User::markActive);
    }
}
