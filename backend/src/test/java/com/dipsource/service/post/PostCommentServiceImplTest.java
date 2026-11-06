package com.dipsource.service.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.post.PostCommentResponse;
import com.dipsource.dto.post.WriteCommentRequest;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostCategory;
import com.dipsource.entity.post.PostComment;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.post.PostCommentLikeRepository;
import com.dipsource.repository.post.PostCommentRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.service.post.impl.PostCommentServiceImpl;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceImplTest {

    private static final Long POST_OWNER = 1L;
    private static final Long WRITER = 2L;
    private static final Long STRANGER = 3L;

    @Mock private PostRepository postRepository;
    @Mock private PostCommentRepository postCommentRepository;
    @Mock private PostCommentLikeRepository postCommentLikeRepository;
    @Mock private UserService userService;

    private PostCommentServiceImpl service;
    private Post post;

    @BeforeEach
    void setUp() {
        service = new PostCommentServiceImpl(postRepository, postCommentRepository, postCommentLikeRepository, userService);
        post = withId(Post.builder().userId(POST_OWNER).title("글").category(PostCategory.BACKEND)
                .content("본문은 스무 자를 넘겨야 저장할 수 있어요.").build(), 10L);
    }

    @Nested
    class DeletedPost {

        @Test
        void noCommentsOnDeletedPost() {
            post.delete();
            when(postRepository.findById(10L)).thenReturn(Optional.of(post));

            assertThatThrownBy(() -> service.create(WRITER, 10L, new WriteCommentRequest("댓글", null)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOUND);
            assertThatThrownBy(() -> service.getComments(10L, WRITER, PageRequest.of(0, 50)))
                    .isInstanceOf(BusinessException.class);
            verify(postCommentRepository, never()).save(any());
        }

        @Test
        void noEditDeleteOrLikeOnDeletedPostComment() {
            PostComment comment = comment(20L, WRITER, null);
            post.delete();
            when(postCommentRepository.findById(20L)).thenReturn(Optional.of(comment));

            assertThatThrownBy(() -> service.update(WRITER, 20L, "수정")).isInstanceOf(BusinessException.class);
            assertThatThrownBy(() -> service.delete(WRITER, 20L)).isInstanceOf(BusinessException.class);
            assertThatThrownBy(() -> service.like(WRITER, 20L)).isInstanceOf(BusinessException.class);
            verify(postCommentLikeRepository, never()).insertIgnore(anyLong(), anyLong());
        }
    }

    @Nested
    class Create {

        @Test
        void commentIncreasesPostCommentCount() {
            when(postRepository.findById(10L)).thenReturn(Optional.of(post));
            when(userService.isActiveUser(WRITER)).thenReturn(true);
            when(postCommentRepository.save(any(PostComment.class))).thenAnswer(inv -> withId(inv.getArgument(0), 20L));

            assertThat(service.create(WRITER, 10L, new WriteCommentRequest("  좋은 글 감사해요  ", null)).id()).isEqualTo(20L);
            verify(postRepository).incrementCommentCount(10L);
        }

        @Test
        void replyToReplyIsRejected() {
            PostComment top = comment(20L, WRITER, null);
            PostComment reply = comment(21L, STRANGER, top);
            when(postRepository.findById(10L)).thenReturn(Optional.of(post));
            when(userService.isActiveUser(WRITER)).thenReturn(true);
            when(postCommentRepository.findById(21L)).thenReturn(Optional.of(reply));

            assertThatThrownBy(() -> service.create(WRITER, 10L, new WriteCommentRequest("답글의 답글", 21L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.BAD_REQUEST);
        }

        @Test
        void replyToOtherPostCommentIsRejected() {
            Post other = withId(Post.builder().userId(POST_OWNER).title("다른 글").category(PostCategory.AI)
                    .content("다른 글 본문도 스무 자를 넘겨야 해요.").build(), 99L);
            PostComment foreign = withId(PostComment.builder().post(other).userId(STRANGER).content("남의 글 댓글").build(), 30L);
            when(postRepository.findById(10L)).thenReturn(Optional.of(post));
            when(userService.isActiveUser(WRITER)).thenReturn(true);
            when(postCommentRepository.findById(30L)).thenReturn(Optional.of(foreign));

            assertThatThrownBy(() -> service.create(WRITER, 10L, new WriteCommentRequest("답글", 30L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.BAD_REQUEST);
            verify(postCommentRepository, never()).save(any());
        }
    }

    @Nested
    class Permissions {

        @Test
        void onlyWriterCanEdit() {
            when(postCommentRepository.findById(20L)).thenReturn(Optional.of(comment(20L, WRITER, null)));

            assertThatThrownBy(() -> service.update(POST_OWNER, 20L, "글 주인도 남의 댓글은 못 고쳐요"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FORBIDDEN);
        }

        @Test
        void postOwnerCanDeleteButStrangerCannot() {
            PostComment comment = comment(20L, WRITER, null);
            when(postCommentRepository.findById(20L)).thenReturn(Optional.of(comment));

            assertThatThrownBy(() -> service.delete(STRANGER, 20L)).isInstanceOf(BusinessException.class);
            service.delete(POST_OWNER, 20L);

            assertThat(comment.getDeletedAt()).isNotNull();
            verify(postRepository).decrementCommentCount(10L);
        }
    }

    @Nested
    class Likes {

        @Test
        void commentLikeIsIdempotent() {
            when(postCommentRepository.findById(20L)).thenReturn(Optional.of(comment(20L, WRITER, null)));
            when(postCommentLikeRepository.insertIgnore(STRANGER, 20L)).thenReturn(1, 0);

            service.like(STRANGER, 20L);
            service.like(STRANGER, 20L);

            verify(postCommentRepository, times(1)).incrementLikeCount(20L);
        }
    }

    @Nested
    class Listing {

        @Test
        void repliesAreNestedAndDeletedParentShowsPlaceholder() {
            PostComment top = comment(20L, WRITER, null);
            top.delete();
            PostComment reply = comment(21L, STRANGER, top);
            when(postRepository.findById(10L)).thenReturn(Optional.of(post));
            when(postCommentRepository.findTopLevel(any(Long.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(top), PageRequest.of(0, 50), 1));
            when(postCommentRepository.findReplies(List.of(20L))).thenReturn(List.of(reply));
            when(userService.getSummaries(List.of(WRITER, STRANGER))).thenReturn(Map.of(
                    WRITER, new UserSummaryResponse(WRITER, "작성자", null, List.of()),
                    STRANGER, new UserSummaryResponse(STRANGER, "답글러", null, List.of())));
            when(postCommentLikeRepository.findLikedCommentIds(STRANGER, List.of(20L, 21L))).thenReturn(List.of(21L));

            List<PostCommentResponse> content = service.getComments(10L, STRANGER, PageRequest.of(0, 50)).content();

            assertThat(content).hasSize(1);
            assertThat(content.get(0).deleted()).isTrue();
            assertThat(content.get(0).content()).isEqualTo(PostCommentResponse.DELETED_CONTENT);
            assertThat(content.get(0).replies()).extracting(PostCommentResponse::id).containsExactly(21L);
            assertThat(content.get(0).replies().get(0).liked()).isTrue();
        }
    }

    private PostComment comment(Long id, Long userId, PostComment parent) {
        return withId(PostComment.builder().post(post).userId(userId).parent(parent).content("댓글 " + id).build(), id);
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
