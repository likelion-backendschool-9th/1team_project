package com.dipsource.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.notification.NotificationResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.notification.Notification;
import com.dipsource.entity.notification.NotificationType;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.notification.NotificationRepository;
import com.dipsource.service.notification.impl.NotificationServiceImpl;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class NotificationServiceImplTest {

    private NotificationRepository repository;
    private UserService userService;
    private BlockRepository blockRepository;
    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(NotificationRepository.class);
        userService = mock(UserService.class);
        blockRepository = mock(BlockRepository.class);
        service = new NotificationServiceImpl(repository, userService, blockRepository);
    }

    @Test
    void savesNotificationForOtherUser() {
        service.notify(1L, 2L, NotificationType.FOLLOW, null, null);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getReceiverId()).isEqualTo(1L);
        assertThat(captor.getValue().getActorId()).isEqualTo(2L);
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.FOLLOW);
        assertThat(captor.getValue().isRead()).isFalse();
    }

    @Test
    void groupsRepeatedGuestbookNotifications() {
        when(repository.existsByReceiverIdAndActorIdAndTypeAndReadFalseAndCreatedAtAfter(
                eq(1L), eq(2L), eq(NotificationType.GUESTBOOK), any())).thenReturn(true);

        service.notify(1L, 2L, NotificationType.GUESTBOOK, NotificationService.TARGET_MINI_HOME, 1L);

        verify(repository, never()).save(any());
    }

    @Test
    void savesGuestbookNotificationWhenNoRecentUnread() {
        service.notify(1L, 2L, NotificationType.GUESTBOOK, NotificationService.TARGET_MINI_HOME, 1L);

        verify(repository).save(any());
    }

    @Test
    void doesNotGroupOtherTypes() {
        service.notify(1L, 2L, NotificationType.SPONSOR, NotificationService.TARGET_MINI_HOME, 1L);

        verify(repository).save(any());
        verify(repository, never()).existsByReceiverIdAndActorIdAndTypeAndReadFalseAndCreatedAtAfter(
                any(), any(), any(), any());
    }

    @Test
    void skipsNotificationToMyself() {
        service.notify(1L, 1L, NotificationType.GUESTBOOK, NotificationService.TARGET_MINI_HOME, 1L);

        verify(repository, never()).save(any());
    }

    @Test
    void skipsNotificationBetweenBlockedUsers() {
        when(blockRepository.existsBetween(2L, 1L)).thenReturn(true);

        service.notify(1L, 2L, NotificationType.FOLLOW, null, null);

        verify(repository, never()).save(any());
    }

    @Test
    void listsWithActorMessageAndHref() {
        Notification follow = notification(NotificationType.FOLLOW, 2L, null, null);
        Notification like = notification(NotificationType.LIKE, 3L, NotificationService.TARGET_POST, 9L);
        Notification system = notification(NotificationType.SPONSOR, null, null, null);
        when(repository.findByReceiverIdOrderByCreatedAtDescIdDesc(anyLong(), any()))
                .thenReturn(new PageImpl<>(List.of(follow, like, system), PageRequest.of(0, 20), 3));
        when(userService.getSummaries(List.of(2L, 3L))).thenReturn(Map.of(
                2L, new UserSummaryResponse(2L, "팔로워", null, List.of())
        ));

        PageResponse<NotificationResponse> page = service.list(1L, PageRequest.of(0, 20));

        NotificationResponse first = page.content().get(0);
        assertThat(first.type()).isEqualTo("follow");
        assertThat(first.actor().nickname()).isEqualTo("팔로워");
        assertThat(first.message()).isEqualTo("님이 나를 팔로우하기 시작했어요.");
        assertThat(first.href()).isEqualTo("/mini-home/2");

        NotificationResponse second = page.content().get(1);
        assertThat(second.actor().nickname()).isEqualTo("삭제된 유저");
        assertThat(second.message()).contains("블로그 글");
        assertThat(second.href()).isEqualTo("/blog/posts/9");

        assertThat(page.content().get(2).actor().nickname()).isEqualTo("딥소스");
    }

    @Test
    void marksOwnNotificationRead() {
        Notification item = notification(NotificationType.FOLLOW, 2L, null, null);
        when(repository.findByIdAndReceiverId(10L, 1L)).thenReturn(Optional.of(item));

        service.markRead(1L, 10L);

        assertThat(item.isRead()).isTrue();
    }

    @Test
    void rejectsReadingSomeoneElsesNotification() {
        when(repository.findByIdAndReceiverId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markRead(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void countsUnread() {
        when(repository.countByReceiverIdAndReadFalse(1L)).thenReturn(3L);

        assertThat(service.countUnread(1L)).isEqualTo(3L);
    }

    private static Notification notification(NotificationType type, Long actorId, String targetType, Long targetId) {
        return Notification.builder()
                .receiverId(1L)
                .actorId(actorId)
                .type(type)
                .targetType(targetType)
                .targetId(targetId)
                .build();
    }
}
