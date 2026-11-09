package com.dipsource.service.follow.impl;

import com.dipsource.dto.follow.FollowPresenceResponse;
import com.dipsource.dto.follow.FollowUserResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.notification.NotificationType;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.service.follow.FollowService;
import com.dipsource.service.notification.NotificationService;
import com.dipsource.service.user.UserService;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link FollowService} 구현체. */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    /** 목록 한 번에 최대 몇 명까지 주는지 */
    private static final int MAX_PAGE_SIZE = 100;

    /** 닉네임 검색어 최대 길이 (닉네임 컬럼 길이와 같다) */
    private static final int MAX_QUERY_LENGTH = 30;

    private final FollowRepository followRepository;
    private final UserService userService;
    private final BlockRepository blockRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void follow(Long followerId, Long targetId) {
        requireOther(followerId, targetId);
        // 차단 여부가 드러나지 않도록 없는 회원과 같은 응답을 준다.
        if (!userService.isActiveUser(targetId) || blockRepository.existsBetween(followerId, targetId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "팔로우할 수 없는 회원이에요.");
        }
        // 동시에 두 번 눌러도 한 줄만 남도록 DB의 중복 무시(ON CONFLICT)에 맡긴다.
        // 새로 팔로우했을 때만 알림을 보낸다. 이미 팔로우 중이면(0줄) 보내지 않는다.
        if (followRepository.insertIgnore(followerId, targetId) > 0) {
            notificationService.notify(targetId, followerId, NotificationType.FOLLOW, null, null);
        }
    }

    @Override
    @Transactional
    public void unfollow(Long followerId, Long targetId) {
        requireOther(followerId, targetId);
        // 탈퇴·정지한 회원도 목록에서 정리할 수 있도록 상대 상태는 보지 않는다.
        followRepository.deleteByFollowerIdAndFollowingId(followerId, targetId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FollowUserResponse> getFollowings(Long userId, String query, Pageable pageable) {
        Page<Long> page = followRepository.findFollowingIds(
                userId, UserStatus.ACTIVE, nicknamePattern(query), limit(pageable));
        List<Long> ids = page.getContent();
        // 팔로잉 목록이니 내가 팔로우 중인 건 확실하고, 상대가 나를 팔로우하는지만 확인한다.
        Set<Long> followsMe = ids.isEmpty() ? Set.of() : new HashSet<>(followRepository.findFollowerIdsIn(userId, ids));
        return toPage(page, id -> true, followsMe::contains);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FollowUserResponse> getFollowers(Long userId, String query, Pageable pageable) {
        Page<Long> page = followRepository.findFollowerIds(
                userId, UserStatus.ACTIVE, nicknamePattern(query), limit(pageable));
        List<Long> ids = page.getContent();
        // 팔로워 목록이니 상대가 나를 팔로우하는 건 확실하고, 내가 맞팔 중인지만 확인한다.
        Set<Long> following = ids.isEmpty() ? Set.of() : new HashSet<>(followRepository.findFollowingIdsIn(userId, ids));
        return toPage(page, following::contains, id -> true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowPresenceResponse> getFollowingPresence(Long userId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        List<Long> requested = userIds.stream().distinct().limit(MAX_PAGE_SIZE).toList();
        // 목록과 같은 기준: 내가 팔로우 중이고 활동 중인 회원만 본다.
        Set<Long> followed = new HashSet<>(
                followRepository.findFollowingIdsInWithStatus(userId, requested, UserStatus.ACTIVE));
        if (followed.isEmpty()) {
            return List.of();
        }
        Set<Long> online = userService.getOnlineUserIds(followed);
        // 요청한 순서(화면 순서)를 그대로 지킨다.
        return requested.stream()
                .filter(followed::contains)
                .map(id -> new FollowPresenceResponse(id, online.contains(id)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMutualFollow(Long userId, Long targetId) {
        if (userId == null || targetId == null || userId.equals(targetId)) {
            return false;
        }
        // 내가 상대를, 상대가 나를 팔로우한 기록이 둘 다 있어야 맞팔이다.
        return followRepository.existsByIdFollowerIdAndIdFollowingId(userId, targetId)
                && followRepository.existsByIdFollowerIdAndIdFollowingId(targetId, userId);
    }

    private PageResponse<FollowUserResponse> toPage(
            Page<Long> page,
            Predicate<Long> following,
            Predicate<Long> followsMe
    ) {
        Map<Long, UserSummaryResponse> users = userService.getSummaries(page.getContent());
        // 쿼리가 정한 순서(최근 팔로우 순)를 그대로 지킨다.
        List<FollowUserResponse> content = page.getContent().stream()
                .filter(users::containsKey)
                .map(id -> FollowUserResponse.of(users.get(id), following.test(id), followsMe.test(id)))
                .toList();
        return PageResponse.of(page, content);
    }

    /** 닉네임 검색어를 대소문자 구분 없는 "포함" LIKE 패턴으로 바꾼다. 검색어의 %, _는 글자 그대로 찾는다. */
    public static String nicknamePattern(String query) {
        if (query == null || query.isBlank()) {
            return "%";
        }
        String trimmed = query.strip();
        if (trimmed.length() > MAX_QUERY_LENGTH) {
            trimmed = trimmed.substring(0, MAX_QUERY_LENGTH);
        }
        String escaped = trimmed.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    private static Pageable limit(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE));
    }

    private static void requireOther(Long followerId, Long targetId) {
        if (targetId == null || targetId.equals(followerId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "나 자신은 팔로우할 수 없어요.");
        }
    }
}
