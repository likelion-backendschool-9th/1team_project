package com.dipsource.service.follow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dipsource.dto.follow.FollowPresenceResponse;
import com.dipsource.dto.follow.FollowUserResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.entity.notification.NotificationType;
import com.dipsource.service.follow.impl.FollowServiceImpl;
import com.dipsource.service.notification.NotificationService;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository repository;

    @Mock
    private UserService userService;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private NotificationService notificationService;

    private FollowServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FollowServiceImpl(repository, userService, blockRepository, notificationService);
    }

    @Nested
    class Follow {

        @Test
        void activeUserIsFollowed() {
            when(userService.isActiveUser(2L)).thenReturn(true);

            service.follow(1L, 2L);

            verify(repository).insertIgnore(1L, 2L);
        }

        @Test
        void newFollowSendsNotification() {
            when(userService.isActiveUser(2L)).thenReturn(true);
            when(repository.insertIgnore(1L, 2L)).thenReturn(1);

            service.follow(1L, 2L);

            verify(notificationService).notify(2L, 1L, NotificationType.FOLLOW, null, null);
        }

        @Test
        void alreadyFollowingSendsNoNotification() {
            when(userService.isActiveUser(2L)).thenReturn(true);
            when(repository.insertIgnore(1L, 2L)).thenReturn(0);

            service.follow(1L, 2L);

            verifyNoInteractions(notificationService);
        }

        @Test
        void alreadyFollowingDoesNotFail() {
            when(userService.isActiveUser(2L)).thenReturn(true);
            when(repository.insertIgnore(1L, 2L)).thenReturn(0);

            service.follow(1L, 2L);

            verify(repository).insertIgnore(1L, 2L);
        }

        @Test
        void blockedRelationIsRejected() {
            when(userService.isActiveUser(2L)).thenReturn(true);
            when(blockRepository.existsBetween(1L, 2L)).thenReturn(true);

            assertThatThrownBy(() -> service.follow(1L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOUND);
            verify(repository, never()).insertIgnore(any(), any());
        }

        @Test
        void selfFollowIsRejected() {
            assertThatThrownBy(() -> service.follow(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.BAD_REQUEST);
            verifyNoInteractions(repository, userService);
        }

        @Test
        void inactiveUserIsRejected() {
            when(userService.isActiveUser(2L)).thenReturn(false);

            assertThatThrownBy(() -> service.follow(1L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOUND);
            verify(repository, never()).insertIgnore(any(), any());
        }
    }

    @Nested
    class Unfollow {

        @Test
        void followingIsDeleted() {
            service.unfollow(1L, 2L);

            verify(repository).deleteByFollowerIdAndFollowingId(1L, 2L);
        }

        @Test
        void notFollowingDoesNotFail() {
            when(repository.deleteByFollowerIdAndFollowingId(1L, 2L)).thenReturn(0);

            service.unfollow(1L, 2L);

            verify(repository).deleteByFollowerIdAndFollowingId(1L, 2L);
        }

        @Test
        void selfUnfollowIsRejected() {
            assertThatThrownBy(() -> service.unfollow(1L, 1L))
                    .isInstanceOf(BusinessException.class);
            verifyNoInteractions(repository);
        }
    }

    @Nested
    class Lists {

        private final PageRequest firstPage = PageRequest.of(0, 20);

        @Test
        void followingsKeepOrderAndMarkFollowsMe() {
            when(repository.findFollowingIds(1L, UserStatus.ACTIVE, "%", firstPage))
                    .thenReturn(new PageImpl<>(List.of(3L, 2L), firstPage, 2));
            when(repository.findFollowerIdsIn(1L, List.of(3L, 2L))).thenReturn(List.of(2L));
            when(userService.getSummaries(List.of(3L, 2L))).thenReturn(Map.of(2L, user(2L), 3L, user(3L)));

            List<FollowUserResponse> content = service.getFollowings(1L, null, firstPage).content();

            assertThat(content).extracting(FollowUserResponse::id).containsExactly(3L, 2L);
            assertThat(content).allMatch(FollowUserResponse::following);
            assertThat(content).extracting(FollowUserResponse::followsMe).containsExactly(false, true);
        }

        @Test
        void followersMarkMutualFollow() {
            when(repository.findFollowerIds(1L, UserStatus.ACTIVE, "%", firstPage))
                    .thenReturn(new PageImpl<>(List.of(4L, 5L), firstPage, 2));
            when(repository.findFollowingIdsIn(1L, List.of(4L, 5L))).thenReturn(List.of(5L));
            when(userService.getSummaries(List.of(4L, 5L))).thenReturn(Map.of(4L, user(4L), 5L, user(5L)));

            List<FollowUserResponse> content = service.getFollowers(1L, null, firstPage).content();

            assertThat(content).allMatch(FollowUserResponse::followsMe);
            assertThat(content).extracting(FollowUserResponse::following).containsExactly(false, true);
        }

        @Test
        void emptyListSkipsExtraQueries() {
            when(repository.findFollowingIds(1L, UserStatus.ACTIVE, "%", firstPage))
                    .thenReturn(new PageImpl<>(List.of(), firstPage, 0));
            when(userService.getSummaries(List.of())).thenReturn(Map.of());

            assertThat(service.getFollowings(1L, null, firstPage).content()).isEmpty();
            verify(repository, never()).findFollowerIdsIn(any(), any());
        }

        @Test
        void pageSizeIsCappedAt100() {
            PageRequest huge = PageRequest.of(0, 1000);
            when(repository.findFollowingIds(1L, UserStatus.ACTIVE, "%", PageRequest.of(0, 100)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));
            when(userService.getSummaries(List.of())).thenReturn(Map.of());

            assertThat(service.getFollowings(1L, null, huge).size()).isEqualTo(100);
        }

        @Test
        void presenceOnlyForActiveFollowedIdsInRequestOrder() {
            when(repository.findFollowingIdsInWithStatus(1L, List.of(3L, 9L, 2L), UserStatus.ACTIVE))
                    .thenReturn(List.of(2L, 3L));
            when(userService.getOnlineUserIds(Set.of(2L, 3L))).thenReturn(Set.of(3L));

            assertThat(service.getFollowingPresence(1L, List.of(3L, 9L, 2L))).containsExactly(
                    new FollowPresenceResponse(3L, true),
                    new FollowPresenceResponse(2L, false));
        }

        @Test
        void presenceWithoutIdsSkipsQueries() {
            assertThat(service.getFollowingPresence(1L, null)).isEmpty();
            assertThat(service.getFollowingPresence(1L, List.of())).isEmpty();
            verifyNoInteractions(repository, userService);
        }

        @Test
        void presenceChecksAtMost100Ids() {
            List<Long> many = LongStream.rangeClosed(1, 150).boxed().toList();
            when(repository.findFollowingIdsInWithStatus(1L, many.subList(0, 100), UserStatus.ACTIVE))
                    .thenReturn(List.of());

            assertThat(service.getFollowingPresence(1L, many)).isEmpty();
            verifyNoInteractions(userService);
        }

        @Test
        void searchQueryIsPassedAsLikePattern() {
            when(repository.findFollowerIds(1L, UserStatus.ACTIVE, "%시드%", firstPage))
                    .thenReturn(new PageImpl<>(List.of(), firstPage, 0));
            when(userService.getSummaries(List.of())).thenReturn(Map.of());

            assertThat(service.getFollowers(1L, "  시드 ", firstPage).content()).isEmpty();
        }

        @Test
        void nicknamePatternIgnoresCaseAndEscapesWildcards() {
            assertThat(FollowServiceImpl.nicknamePattern(null)).isEqualTo("%");
            assertThat(FollowServiceImpl.nicknamePattern("   ")).isEqualTo("%");
            assertThat(FollowServiceImpl.nicknamePattern("Dev")).isEqualTo("%dev%");
            assertThat(FollowServiceImpl.nicknamePattern("50%_a\\b")).isEqualTo("%50\\%\\_a\\\\b%");
            assertThat(FollowServiceImpl.nicknamePattern("a".repeat(40))).isEqualTo("%" + "a".repeat(30) + "%");
        }

        private UserSummaryResponse user(Long id) {
            return new UserSummaryResponse(id, "user" + id, null, List.of());
        }
    }

    @Nested
    class MutualFollow {

        @Test
        void bothFollowingIsMutual() {
            when(repository.existsByIdFollowerIdAndIdFollowingId(1L, 2L)).thenReturn(true);
            when(repository.existsByIdFollowerIdAndIdFollowingId(2L, 1L)).thenReturn(true);

            assertThat(service.isMutualFollow(1L, 2L)).isTrue();
        }

        @Test
        void oneWayFollowIsNotMutual() {
            when(repository.existsByIdFollowerIdAndIdFollowingId(1L, 2L)).thenReturn(true);
            when(repository.existsByIdFollowerIdAndIdFollowingId(2L, 1L)).thenReturn(false);

            assertThat(service.isMutualFollow(1L, 2L)).isFalse();
        }

        @Test
        void notFollowingSkipsReverseCheck() {
            when(repository.existsByIdFollowerIdAndIdFollowingId(1L, 2L)).thenReturn(false);

            assertThat(service.isMutualFollow(1L, 2L)).isFalse();
            verify(repository, never()).existsByIdFollowerIdAndIdFollowingId(2L, 1L);
        }

        @Test
        void sameUserOrMissingIdIsNotMutual() {
            assertThat(service.isMutualFollow(1L, 1L)).isFalse();
            assertThat(service.isMutualFollow(null, 2L)).isFalse();
            assertThat(service.isMutualFollow(1L, null)).isFalse();
            verify(repository, never()).existsByIdFollowerIdAndIdFollowingId(any(), any());
        }
    }
}
