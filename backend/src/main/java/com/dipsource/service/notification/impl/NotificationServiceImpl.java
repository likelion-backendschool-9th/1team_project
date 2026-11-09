package com.dipsource.service.notification.impl;

import com.dipsource.dto.notification.NotificationResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.notification.Notification;
import com.dipsource.entity.notification.NotificationType;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.notification.NotificationRepository;
import com.dipsource.service.notification.NotificationService;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link NotificationService} 구현체. */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final UserSummaryResponse SYSTEM = new UserSummaryResponse(null, "딥소스", null, List.of());

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final BlockRepository blockRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByReceiverIdOrderByCreatedAtDescIdDesc(userId, pageable);
        List<Long> actorIds = page.getContent().stream()
                .map(Notification::getActorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UserSummaryResponse> actors = actorIds.isEmpty() ? Map.of() : userService.getSummaries(actorIds);
        return PageResponse.of(page, item -> toResponse(item, actors));
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByReceiverIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "알림을 찾을 수 없습니다"));
        notification.markRead();
    }

    @Override
    @Transactional
    public void notify(Long receiverId, Long actorId, NotificationType type, String targetType, Long targetId) {
        if (receiverId == null || type == null || receiverId.equals(actorId)) {
            return;
        }
        if (actorId != null && blockRepository.existsBetween(actorId, receiverId)) {
            return;
        }
        notificationRepository.save(Notification.builder()
                .receiverId(receiverId)
                .actorId(actorId)
                .type(type)
                .targetType(targetType)
                .targetId(targetId)
                .build());
    }

    private static NotificationResponse toResponse(Notification item, Map<Long, UserSummaryResponse> actors) {
        UserSummaryResponse actor = SYSTEM;
        if (item.getActorId() != null) {
            actor = actors.getOrDefault(item.getActorId(),
                    new UserSummaryResponse(item.getActorId(), "삭제된 유저", null, List.of()));
        }
        return new NotificationResponse(
                item.getId(),
                item.getType().name().toLowerCase(Locale.ROOT),
                actor,
                message(item),
                href(item),
                item.getCreatedAt(),
                item.isRead()
        );
    }

    private static String message(Notification item) {
        return switch (item.getType()) {
            case LIKE -> "님이 내 " + targetName(item.getTargetType()) + "을(를) 좋아해요.";
            case COMMENT -> "님이 내 " + targetName(item.getTargetType()) + "에 댓글을 남겼어요.";
            case FOLLOW -> "님이 나를 팔로우하기 시작했어요.";
            case GUESTBOOK -> "님이 미니홈피 방명록을 남겼어요.";
            case REPOST -> "님이 내 피드를 리포스트했어요.";
            case SUBSCRIBE -> "님이 내 블로그를 구독했어요.";
            case SPONSOR -> "님이 미니홈피에 칩을 후원했어요.";
        };
    }

    private static String href(Notification item) {
        return switch (item.getType()) {
            case FOLLOW -> item.getActorId() == null ? "/" : "/mini-home/" + item.getActorId();
            case GUESTBOOK -> "/mini-home/" + item.getReceiverId() + "/guestbook";
            case SUBSCRIBE -> "/blog/" + item.getReceiverId();
            case SPONSOR -> "/my/coins";
            case LIKE, COMMENT, REPOST -> targetHref(item.getTargetType(), item.getTargetId());
        };
    }

    private static String targetName(String targetType) {
        if (targetType == null) {
            return "글";
        }
        return switch (targetType) {
            case TARGET_FEED -> "피드";
            case TARGET_POST -> "블로그 글";
            case TARGET_DEMO -> "데모";
            case TARGET_MINI_HOME -> "미니홈피";
            default -> "글";
        };
    }

    private static String targetHref(String targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            return "/";
        }
        return switch (targetType) {
            case TARGET_FEED -> "/feeds/" + targetId;
            case TARGET_POST -> "/blog/posts/" + targetId;
            case TARGET_DEMO -> "/demos/" + targetId;
            case TARGET_MINI_HOME -> "/mini-home/" + targetId;
            default -> "/";
        };
    }
}
