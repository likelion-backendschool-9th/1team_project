package com.dipsource.controller.notification;

import com.dipsource.dto.notification.NotificationResponse;
import com.dipsource.dto.notification.UnreadCountResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 내 알림 조회·읽음 처리. */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.of(notificationService.list(requireUserId(principal), pageable));
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.of(new UnreadCountResponse(notificationService.countUnread(requireUserId(principal))));
    }

    @PutMapping("/{notificationId}/read")
    public ApiResponse<Void> markRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long notificationId
    ) {
        notificationService.markRead(requireUserId(principal), notificationId);
        return ApiResponse.of(null);
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
