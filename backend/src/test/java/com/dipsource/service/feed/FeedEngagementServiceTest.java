package com.dipsource.service.feed;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedStatus;
import com.dipsource.entity.feed.FeedUserId;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.feed.FeedBookmarkRepository;
import com.dipsource.repository.feed.FeedLikeRepository;
import com.dipsource.repository.feed.FeedRepository;
import com.dipsource.repository.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedEngagementServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long FEED_ID = 42L;

    @Mock FeedRepository feedRepository;
    @Mock FeedLikeRepository feedLikeRepository;
    @Mock FeedBookmarkRepository feedBookmarkRepository;
    @Mock UserRepository userRepository;
    @Mock TagWeightService tagWeightService;
    @InjectMocks FeedEngagementService service;

    @Nested
    @DisplayName("like")
    class Like {

        @Test
        @DisplayName("ACTIVE 피드에 처음 좋아요하면 count·태그 가중치를 올린다")
        void firstLike() {
            when(feedRepository.findById(FEED_ID)).thenReturn(Optional.of(activeFeed()));
            when(feedLikeRepository.insertIgnore(USER_ID, FEED_ID)).thenReturn(1);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));

            service.like(USER_ID, FEED_ID);

            verify(feedRepository).incrementLikeCount(FEED_ID);
            verify(tagWeightService).increaseWeight(any(User.class), eq(FEED_ID), eq(1));
        }

        @Test
        @DisplayName("이미 좋아요된 상태면 count·가중치를 건드리지 않는다 (멱등)")
        void alreadyLiked() {
            when(feedRepository.findById(FEED_ID)).thenReturn(Optional.of(activeFeed()));
            when(feedLikeRepository.insertIgnore(USER_ID, FEED_ID)).thenReturn(0);

            service.like(USER_ID, FEED_ID);

            verify(feedRepository, never()).incrementLikeCount(any());
            verifyNoInteractions(tagWeightService);
        }

        @Test
        @DisplayName("없는/삭제된 피드는 NOT_FOUND")
        void missingFeed() {
            when(feedRepository.findById(FEED_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.like(USER_ID, FEED_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOUND);

            verify(feedLikeRepository, never()).insertIgnore(any(), any());
        }

        @Test
        @DisplayName("HIDDEN 피드도 NOT_FOUND")
        void hiddenFeed() {
            Feed feed = activeFeed();
            feed.hide();
            when(feedRepository.findById(FEED_ID)).thenReturn(Optional.of(feed));

            assertThatThrownBy(() -> service.like(USER_ID, FEED_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("unlike")
    class Unlike {

        @Test
        @DisplayName("좋아요가 있으면 삭제하고 count·가중치를 내린다")
        void unlikeExisting() {
            when(feedRepository.findById(FEED_ID)).thenReturn(Optional.of(activeFeed()));
            when(feedLikeRepository.existsById(new FeedUserId(USER_ID, FEED_ID))).thenReturn(true);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));

            service.unlike(USER_ID, FEED_ID);

            verify(feedLikeRepository).deleteById(new FeedUserId(USER_ID, FEED_ID));
            verify(feedRepository).decrementLikeCount(FEED_ID);
            verify(tagWeightService).decreaseWeight(any(User.class), eq(FEED_ID), eq(1));
        }

        @Test
        @DisplayName("좋아요가 없으면 no-op (멱등)")
        void unlikeMissing() {
            when(feedRepository.findById(FEED_ID)).thenReturn(Optional.of(activeFeed()));
            when(feedLikeRepository.existsById(new FeedUserId(USER_ID, FEED_ID))).thenReturn(false);

            service.unlike(USER_ID, FEED_ID);

            verify(feedLikeRepository, never()).deleteById(any());
            verify(feedRepository, never()).decrementLikeCount(any());
            verifyNoInteractions(tagWeightService);
        }
    }

    @Nested
    @DisplayName("bookmark")
    class Bookmark {

        @Test
        @DisplayName("처음 북마크하면 태그 가중치만 올린다 (like_count는 안 건드림)")
        void firstBookmark() {
            when(feedRepository.findById(FEED_ID)).thenReturn(Optional.of(activeFeed()));
            when(feedBookmarkRepository.insertIgnore(USER_ID, FEED_ID)).thenReturn(1);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));

            service.bookmark(USER_ID, FEED_ID);

            verify(tagWeightService).increaseWeight(any(User.class), eq(FEED_ID), eq(1));
            verify(feedRepository, never()).incrementLikeCount(any());
        }

        @Test
        @DisplayName("이미 북마크면 가중치를 건드리지 않는다 (멱등)")
        void alreadyBookmarked() {
            when(feedRepository.findById(FEED_ID)).thenReturn(Optional.of(activeFeed()));
            when(feedBookmarkRepository.insertIgnore(USER_ID, FEED_ID)).thenReturn(0);

            service.bookmark(USER_ID, FEED_ID);

            verifyNoInteractions(tagWeightService);
        }

        @Test
        @DisplayName("없는 피드는 NOT_FOUND")
        void missingFeed() {
            when(feedRepository.findById(FEED_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.bookmark(USER_ID, FEED_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOUND);

            verify(feedBookmarkRepository, never()).insertIgnore(any(), any());
        }
    }

    @Nested
    @DisplayName("unbookmark")
    class Unbookmark {

        @Test
        @DisplayName("북마크가 있으면 삭제하고 가중치를 내린다")
        void unbookmarkExisting() {
            when(feedRepository.findById(FEED_ID)).thenReturn(Optional.of(activeFeed()));
            when(feedBookmarkRepository.existsById(new FeedUserId(USER_ID, FEED_ID))).thenReturn(true);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));

            service.unbookmark(USER_ID, FEED_ID);

            verify(feedBookmarkRepository).deleteById(new FeedUserId(USER_ID, FEED_ID));
            verify(tagWeightService).decreaseWeight(any(User.class), eq(FEED_ID), eq(1));
        }

        @Test
        @DisplayName("북마크가 없으면 no-op (멱등)")
        void unbookmarkMissing() {
            when(feedRepository.findById(FEED_ID)).thenReturn(Optional.of(activeFeed()));
            when(feedBookmarkRepository.existsById(new FeedUserId(USER_ID, FEED_ID))).thenReturn(false);

            service.unbookmark(USER_ID, FEED_ID);

            verify(feedBookmarkRepository, never()).deleteById(any());
            verifyNoInteractions(tagWeightService);
        }
    }

    private static Feed activeFeed() {
        return Feed.builder().userId(1L).content("테스트 피드").build();
    }

    private static User user() {
        return User.builder()
                .email("liker@test.local")
                .nickname("liker")
                .build();
    }
}
