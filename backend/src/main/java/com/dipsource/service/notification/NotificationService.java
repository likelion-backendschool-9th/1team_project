package com.dipsource.service.notification;

import com.dipsource.dto.notification.NotificationResponse;
import com.dipsource.entity.notification.NotificationType;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 알림 조회·읽음 처리와, 다른 기능에서 부르는 알림 생성. */
public interface NotificationService {

    /** notify의 targetType 값. 엔티티 target_type 컬럼(20자)에 그대로 저장된다. */
    String TARGET_FEED = "FEED";
    String TARGET_POST = "POST";
    String TARGET_DEMO = "DEMO";
    String TARGET_MINI_HOME = "MINI_HOME";

    /** 내 알림 목록. 최신순. */
    PageResponse<NotificationResponse> list(Long userId, Pageable pageable);

    long countUnread(Long userId);

    /** 본인 알림만 읽음 처리한다. 남의 알림이거나 없으면 404. */
    void markRead(Long userId, Long notificationId);

    /**
     * 알림을 만든다. 호출한 기능의 트랜잭션 안에서 저장된다.
     * 자기 자신에게 보내는 알림과, 서로 차단한 사이의 알림은 만들지 않는다.
     *
     * @param actorId    알림을 만든 회원. 시스템 알림이면 null
     * @param targetType TARGET_* 중 하나. 대상이 없으면 null
     */
    void notify(Long receiverId, Long actorId, NotificationType type, String targetType, Long targetId);
}
