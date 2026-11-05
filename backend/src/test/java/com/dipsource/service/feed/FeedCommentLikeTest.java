package com.dipsource.service.feed;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedComment;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.feed.FeedCommentLikeRepository;
import com.dipsource.repository.feed.FeedCommentRepository;
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
class FeedCommentLikeTest {

    private static final Long USER_ID = 7L;
    private static final Long COMMENT_ID = 99L;

    @Mock FeedCommentLikeRepository commentLikeRepository;
    @Mock FeedCommentRepository commentRepository;
    @Mock FeedRepository feedRepository;
    @Mock UserRepository userRepository;
    @Mock TagWeightService tagWeightService;
    @InjectMocks FeedCommentService service;

    @Nested
    @DisplayName("likeComment")
    class LikeComment {

        @Test
        @DisplayName("처음 좋아요하면 likeCount를 올린다")
        void firstLike() {
            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(activeComment()));
            when(commentLikeRepository.insertIgnore(USER_ID, COMMENT_ID)).thenReturn(1);

            service.likeComment(USER_ID, COMMENT_ID);

            verify(commentRepository).incrementCommentLikeCount(COMMENT_ID);
        }

        @Test
        @DisplayName("이미 좋아요면 likeCount를 건드리지 않는다 (멱등)")
        void alreadyLiked() {
            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(activeComment()));
            when(commentLikeRepository.insertIgnore(USER_ID, COMMENT_ID)).thenReturn(0);

            service.likeComment(USER_ID, COMMENT_ID);

            verify(commentRepository, never()).incrementCommentLikeCount(COMMENT_ID);
        }

        @Test
        @DisplayName("삭제된 댓글은 좋아요 불가 (404)")
        void deletedComment() {
            FeedComment deleted = activeComment();
            deleted.delete();
            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(deleted));

            assertThatThrownBy(() -> service.likeComment(USER_ID, COMMENT_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 댓글은 404")
        void missingComment() {
            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.likeComment(USER_ID, COMMENT_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("unlikeComment")
    class UnlikeComment {

        @Test
        @DisplayName("좋아요가 있으면 삭제하고 likeCount를 내린다")
        void unlikeExisting() {
            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(activeComment()));
            when(commentLikeRepository.deleteByUserIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(1);

            service.unlikeComment(USER_ID, COMMENT_ID);

            verify(commentRepository).decrementCommentLikeCount(COMMENT_ID);
        }

        @Test
        @DisplayName("좋아요가 없으면 no-op (멱등)")
        void unlikeMissing() {
            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(activeComment()));
            when(commentLikeRepository.deleteByUserIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(0);

            service.unlikeComment(USER_ID, COMMENT_ID);

            verify(commentRepository, never()).decrementCommentLikeCount(COMMENT_ID);
        }

        @Test
        @DisplayName("삭제된 댓글도 좋아요 취소 가능")
        void deletedCommentCanUnlike() {
            FeedComment deleted = activeComment();
            deleted.delete();
            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(deleted));
            when(commentLikeRepository.deleteByUserIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(1);

            service.unlikeComment(USER_ID, COMMENT_ID);

            verify(commentRepository).decrementCommentLikeCount(COMMENT_ID);
        }
    }

    private static FeedComment activeComment() {
        Feed feed = Feed.builder().userId(1L).content("테스트 피드").build();
        return FeedComment.builder()
                .feed(feed)
                .userId(2L)
                .content("테스트 댓글")
                .build();
    }
}
