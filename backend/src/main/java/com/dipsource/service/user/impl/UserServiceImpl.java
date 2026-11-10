package com.dipsource.service.user.impl;

import com.dipsource.dto.user.CompleteProfileRequest;
import com.dipsource.dto.user.NicknameAvailableResponse;
import com.dipsource.dto.user.UpdateProfileRequest;
import com.dipsource.dto.user.UserMeResponse;
import com.dipsource.dto.user.UserProfileResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.tag.Tag;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.UserInterestTag;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.repository.tag.TagRepository;
import com.dipsource.repository.user.UserInterestTagRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.image.ImageService;
import com.dipsource.service.user.AuthService;
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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dipsource.dto.image.ImageUploadResult;
import org.springframework.web.multipart.MultipartFile;


/** {@link UserService} 구현체. */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9_]+$");
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    /** 마지막 활동이 이 시간 안이면 "접속 중"으로 본다. */
    private static final Duration ONLINE_THRESHOLD = Duration.ofMinutes(5);

    private final UserRepository userRepository;
    private final AuthService authService;
    private final TagRepository tagRepository;
    private final UserInterestTagRepository userInterestTagRepository;
    private final FollowRepository followRepository;
    // R2가 꺼진 환경(로컬 등)에서는 ImageService 빈이 없다. 없어도 서버는 떠야 해서 ObjectProvider로 받는다
    private final ObjectProvider<ImageService> imageServiceProvider;



    @Override
    @Transactional(readOnly = true)
    public UserMeResponse getMe(Long userId) {
        return toMeResponse(getUser(userId));
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

        List<String> techStacks = normalizeSlugs(request.techStacks(), "기술 스택");
        List<String> interestTags = normalizeSlugs(request.interestTags(), "관심 태그");
        if (techStacks.size() < 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "기술 스택을 2개 이상 골라 주세요.");
        }
        if (interestTags.size() < 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "관심 태그를 2개 이상 골라 주세요.");
        }

        user.updateProfile(nickname, user.getProfileImage(), bio, techStacks, user.getGithubUsername());

        // 관심 태그는 통째로 교체한다.
        userInterestTagRepository.deleteAllByUserId(user.getId());
        for (String slug : interestTags) {
            Tag tag = tagRepository.findByName(slug)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(slug).build()));
            userInterestTagRepository.save(new UserInterestTag(user, tag.getId()));
        }

        return toMeResponse(user);
    }

    @Override
    @Transactional
    public UserMeResponse updateMe(Long userId, UpdateProfileRequest request) {
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

        List<String> techStacks = parseTechStacks(request.techStacks());
        if (techStacks.size() < 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "기술 스택을 2개 이상 골라 주세요.");
        }

        user.updateProfile(
            nickname, 
            user.getProfileImage(), 
            bio, 
            techStacks, 
            parseGithubUsername(request.githubUrl()));
        
        return toMeResponse(user);
    }

    @Override 
    @Transactional
    public UserMeResponse updateProfileImage(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미지 파일을 골라 주세요.");
        }
        User user = getUser(userId);
        String before = user.getProfileImage();

        ImageUploadResult uploaded = imageService().upload(file, "profiles");
        user.changeProfileImage(uploaded.thumbnailUrl());

        deleteIfStored(before);
        return toMeResponse(user);
    }

    @Override
    @Transactional
    public UserMeResponse deleteProfileImage(Long userId) {
        User user = getUser(userId);
        String before = user.getProfileImage();
        user.changeProfileImage(null);
        deleteIfStored(before);
        return toMeResponse(user);
    }

    /** R2가 꺼져 있으면 이미지 기능을 쓸 수 없다고 알려 준다 */
    private ImageService imageService() {
        ImageService service = imageServiceProvider.getIfAvailable();
        if (service == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미지 저장소가 꺼져 있어 프로필 이미지를 바꿀 수 없어요.");
        }
        return service;
    }

    private void deleteIfStored(String imageUrl) {
        ImageService service = imageServiceProvider.getIfAvailable();
        if (service == null || !service.isStoredImage(imageUrl)) {
            return;
        }
        try {
            service.delete(imageUrl);
        } catch (RuntimeException e) {
            // 남은 파일은 용량만 차지할 뿐 화면에는 영향이 없다
        }
    }

    @Override 
    @Transactional 
    public void withdraw(Long userId) {
        User user = getUser(userId);
        user.withdraw();
        authService.revokeAll(userId);
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

    private List<String> parseTechStacks(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String item : raw.split(",")) {
            String stack = item.trim();
            if (!stack.isEmpty()) {
                unique.add(stack);
            }
        }
        return List.copyOf(unique);
    }

    private String parseGithubUsername(String githubUrl) {
        if (githubUrl == null || githubUrl.isBlank()) {
            return null;
        }
        String trimmed = githubUrl.trim();
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        String prefix = "https://github.com/";
        if (!trimmed.startsWith(prefix) || trimmed.substring(prefix.length()).contains("/")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "https://github.com/아이디 형식으로 입력해 주세요.");
        }
        String username = trimmed.substring(prefix.length());
        if (username.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "https://github.com/아이디 형식으로 입력해 주세요.");
        }
        return username;
    }

    /** 내 정보 응답. 팔로워·팔로잉 수는 팔로우 목록과 같은 기준(활동 중인 회원만)으로 센다. */
    private UserMeResponse toMeResponse(User user) {
        return UserMeResponse.from(
                user,
                followRepository.countFollowers(user.getId(), UserStatus.ACTIVE),
                followRepository.countFollowings(user.getId(), UserStatus.ACTIVE));
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
