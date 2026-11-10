package com.dipsource.service.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.entity.notification.NotificationType;
import com.dipsource.entity.post.BlogSubscription;
import com.dipsource.entity.post.SubscriptionStatus;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.post.BlogSubscriptionRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.coin.CoinService;
import com.dipsource.service.notification.NotificationService;
import com.dipsource.service.post.impl.BlogSubscriptionServiceImpl;
import com.dipsource.service.user.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BlogSubscriptionServiceImplTest {

    private static final Long ME = 1L;
    private static final Long OWNER = 2L;

    @Mock private BlogSubscriptionRepository repository;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;
    @Mock private BlockRepository blockRepository;
    @Mock private CoinService coinService;
    @Mock private NotificationService notificationService;

    private BlogSubscriptionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BlogSubscriptionServiceImpl(
                repository, userRepository, userService, blockRepository, coinService, notificationService);
    }

    @Nested
    class Subscribe {

        @Test
        void newSubscriptionPaysOneMonthAndNotifiesOwner() {
            stubUsers(3000);
            when(repository.findCurrent(eq(ME), eq(OWNER), any())).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(invocation -> withId(invocation.getArgument(0), 7L));

            service.subscribe(ME, OWNER);

            verify(coinService).payBlogSubscription(ME, 7L, 3000);
            verify(notificationService).notify(OWNER, ME, NotificationType.SUBSCRIBE, null, null);
        }

        @Test
        void alreadySubscribedDoesNotPayAgain() {
            stubUsers(3000);
            when(repository.findCurrent(eq(ME), eq(OWNER), any())).thenReturn(Optional.of(subscription(SubscriptionStatus.ACTIVE)));

            service.subscribe(ME, OWNER);

            verify(repository, never()).save(any());
            verify(coinService, never()).payBlogSubscription(anyLong(), anyLong(), anyInt());
        }

        @Test
        void canceledButNotExpiredIsResumedWithoutPaying() {
            stubUsers(3000);
            BlogSubscription canceled = subscription(SubscriptionStatus.CANCELED);
            when(repository.findCurrent(eq(ME), eq(OWNER), any())).thenReturn(Optional.of(canceled));

            service.subscribe(ME, OWNER);

            assertThat(canceled.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            verify(coinService, never()).payBlogSubscription(anyLong(), anyLong(), anyInt());
        }

        @Test
        void blogWithoutPriceCannotBeSubscribed() {
            stubUsers(0);

            assertThatThrownBy(() -> service.subscribe(ME, OWNER))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.BAD_REQUEST);
            verify(repository, never()).save(any());
        }

        @Test
        void blockedOwnerIsNotFound() {
            stubUsers(3000);
            when(blockRepository.existsBetween(ME, OWNER)).thenReturn(true);

            assertThatThrownBy(() -> service.subscribe(ME, OWNER))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOUND);
        }

        @Test
        void cannotSubscribeToMyOwnBlog() {
            assertThatThrownBy(() -> service.subscribe(ME, ME))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    class Cancel {

        @Test
        void cancelKeepsAccessUntilExpiry() {
            BlogSubscription active = subscription(SubscriptionStatus.ACTIVE);
            when(repository.findById(7L)).thenReturn(Optional.of(active));

            service.cancel(ME, 7L);

            assertThat(active.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
            assertThat(active.getExpiresAt()).isAfter(LocalDateTime.now());
        }

        @Test
        void someoneElsesSubscriptionIsNotFound() {
            when(repository.findById(7L)).thenReturn(Optional.of(subscription(SubscriptionStatus.ACTIVE)));

            assertThatThrownBy(() -> service.cancel(OWNER, 7L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOUND);
        }
    }

    @Nested
    class Mine {

        @Test
        void blockedBlogIsHiddenFromMyList() {
            when(blockRepository.findHiddenUserIds(ME)).thenReturn(List.of(OWNER));
            when(repository.findCurrentBySubscriber(eq(ME), any())).thenReturn(List.of(subscription(SubscriptionStatus.ACTIVE)));

            assertThat(service.getMine(ME)).isEmpty();
            verify(userService, never()).getSummaries(any());
        }
    }

    private void stubUsers(int price) {
        when(userRepository.findByIdForUpdate(ME)).thenReturn(Optional.of(user(ME, 0)));
        when(userRepository.findById(OWNER)).thenReturn(Optional.of(user(OWNER, price)));
    }

    private static User user(Long id, int price) {
        User user = withId(User.builder().email(id + "@test.local").nickname("회원" + id).build(), id);
        user.changeBlogSubscriptionPrice(price);
        return user;
    }

    private static BlogSubscription subscription(SubscriptionStatus status) {
        BlogSubscription subscription = withId(BlogSubscription.builder()
                .subscriberId(ME)
                .blogOwnerId(OWNER)
                .paidPrice(3000)
                .startedAt(LocalDateTime.now().minusDays(3))
                .expiresAt(LocalDateTime.now().plusDays(27))
                .build(), 7L);
        if (status == SubscriptionStatus.CANCELED) {
            subscription.cancel();
        }
        return subscription;
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
