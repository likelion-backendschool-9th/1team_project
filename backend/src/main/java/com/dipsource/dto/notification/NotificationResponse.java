package com.dipsource.dto.notification;

import com.dipsource.dto.user.UserSummaryResponse;
import java.time.LocalDateTime;

/**
 * 알림 한 건. 프론트 AppNotification과 같은 모양이다.
 *
 * @param type    소문자 종류. 예: follow, guestbook
 * @param actor   알림을 만든 회원. 시스템 알림이면 닉네임 "딥소스"
 * @param message 닉네임 뒤에 붙는 문장. 예: "님이 나를 팔로우하기 시작했어요."
 * @param href    누르면 이동할 화면 경로
 */
public record NotificationResponse(
        Long id,
        String type,
        UserSummaryResponse actor,
        String message,
        String href,
        LocalDateTime createdAt,
        boolean read
) {
}
