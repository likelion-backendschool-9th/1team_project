package com.dipsource.service.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.post.PostDetailResponse;
import com.dipsource.dto.post.WritePostRequest;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostCategory;
import com.dipsource.entity.post.PostStatus;
import com.dipsource.entity.post.PostTag;
import com.dipsource.entity.post.SubscriptionStatus;
import com.dipsource.entity.tag.Tag;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.post.BlogSubscriptionRepository;
import com.dipsource.repository.post.PostPurchaseRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.post.PostTagRepository;
import com.dipsource.repository.tag.TagRepository;
import com.dipsource.service.post.impl.PostServiceImpl;
import com.dipsource.service.user.UserService;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    private static final Long AUTHOR = 1L;
    private static final Long OTHER = 2L;
    private static final String BODY = "첫 문단입니다.\n\n둘째 문단입니다.\n\n셋째 문단입니다.\n\n넷째 문단은 잠겨야 합니다.";

    @Mock private PostRepository postRepository;
    @Mock private PostTagRepository postTagRepository;
    @Mock private PostPurchaseRepository postPurchaseRepository;
    @Mock private BlogSubscriptionRepository blogSubscriptionRepository;
    @Mock private TagRepository tagRepository;
    @Mock private UserService userService;
    @Mock private BlockRepository blockRepository;

    private PostServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PostServiceImpl(postRepository, postTagRepository, postPurchaseRepository,
                blogSubscriptionRepository, tagRepository, userService, blockRepository);
    }

    @Nested
    class Create {

        @Test
        void savesPostWithCategoryPriceAndNormalizedTags() {
            when(userService.isActiveUser(AUTHOR)).thenReturn(true);
            when(postRepository.save(any(Post.class))).thenAnswer(inv -> withId(inv.getArgument(0), 10L));
            when(tagRepository.findByName("spring")).thenReturn(Optional.of(tag(5L, "spring")));
            when(tagRepository.findByName("jpa")).thenReturn(Optional.empty());
            when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> withId(inv.getArgument(0), 6L));

            var response = service.create(AUTHOR, request("backend", List.of(" Spring ", "spring", "JPA"), 30));

            ArgumentCaptor<Post> saved = ArgumentCaptor.forClass(Post.class);
            verify(postRepository).save(saved.capture());
            assertThat(response.id()).isEqualTo(10L);
            assertThat(saved.getValue().getCategory()).isEqualTo(PostCategory.BACKEND);
            assertThat(saved.getValue().isPaid()).isTrue();
            assertThat(saved.getValue().getPriceChips()).isEqualTo(30);
            // " Spring "과 "spring"은 같은 태그로 한 번만 붙는다.
            verify(postTagRepository, times(2)).save(any(PostTag.class));
        }

        @Test
        void unknownCategoryIsRejected() {
            when(userService.isActiveUser(AUTHOR)).thenReturn(true);

            assertThatThrownBy(() -> service.create(AUTHOR, request("cooking", List.of(), 0)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.BAD_REQUEST);
            verify(postRepository, never()).save(any());
        }

        @Test
        void inactiveUserCannotWrite() {
            when(userService.isActiveUser(AUTHOR)).thenReturn(false);

            assertThatThrownBy(() -> service.create(AUTHOR, request("backend", List.of(), 0)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FORBIDDEN);
        }
    }

    @Nested
    class UpdateAndDelete {

        @Test
        void onlyAuthorCanUpdate() {
            when(postRepository.findById(10L)).thenReturn(Optional.of(post(AUTHOR, 0)));

            assertThatThrownBy(() -> service.update(OTHER, 10L, request("ai", List.of(), 0)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FORBIDDEN);
            verify(postTagRepository, never()).deleteByPostId(anyLong());
        }

        @Test
        void authorUpdateReplacesFieldsAndTags() {
            Post post = post(AUTHOR, 0);
            when(postRepository.findById(10L)).thenReturn(Optional.of(post));
            when(postRepository.getReferenceById(10L)).thenReturn(post);
            when(tagRepository.findByName("react")).thenReturn(Optional.of(tag(7L, "react")));

            service.update(AUTHOR, 10L, request("frontend", List.of("react"), 0));

            assertThat(post.getCategory()).isEqualTo(PostCategory.FRONTEND);
            verify(postTagRepository).deleteByPostId(10L);
            verify(postTagRepository).save(any(PostTag.class));
        }

        @Test
        void deleteIsSoftAndOnlyByAuthor() {
            Post post = post(AUTHOR, 0);
            when(postRepository.findById(10L)).thenReturn(Optional.of(post));

            assertThatThrownBy(() -> service.delete(OTHER, 10L)).isInstanceOf(BusinessException.class);
            service.delete(AUTHOR, 10L);

            assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
            assertThat(post.getDeletedAt()).isNotNull();
        }

        @Test
        void deletedPostIsNotFound() {
            Post post = post(AUTHOR, 0);
            post.delete();
            when(postRepository.findById(10L)).thenReturn(Optional.of(post));

            assertThatThrownBy(() -> service.getPost(10L, OTHER))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOUND);
        }
    }

    @Nested
    class Detail {

        @Test
        void paidPostIsLockedForGuest() {
            stubDetail(post(AUTHOR, 30));

            PostDetailResponse detail = service.getPost(10L, null);

            assertThat(detail.purchased()).isFalse();
            assertThat(detail.price()).isEqualTo(30);
            assertThat(detail.content()).contains("셋째 문단").doesNotContain("넷째 문단");
            verify(postRepository).incrementViewCount(10L);
        }

        @Test
        void paidPostIsOpenForSubscriber() {
            stubDetail(post(AUTHOR, 30));
            when(blogSubscriptionRepository.existsBySubscriberIdAndBlogOwnerIdAndStatusAndExpiresAtAfter(
                    eq(OTHER), eq(AUTHOR), eq(SubscriptionStatus.ACTIVE), any())).thenReturn(true);

            PostDetailResponse detail = service.getPost(10L, OTHER);

            assertThat(detail.purchased()).isTrue();
            assertThat(detail.content()).contains("넷째 문단");
        }

        @Test
        void authorSeesAllWithoutCountingView() {
            stubDetail(post(AUTHOR, 30));

            PostDetailResponse detail = service.getPost(10L, AUTHOR);

            assertThat(detail.purchased()).isTrue();
            assertThat(detail.content()).contains("넷째 문단");
            verify(postRepository, never()).incrementViewCount(anyLong());
        }

        private void stubDetail(Post post) {
            when(postRepository.findById(10L)).thenReturn(Optional.of(post));
            when(userService.getSummaries(List.of(AUTHOR))).thenReturn(Map.of(AUTHOR, author()));
            when(postTagRepository.findTagNames(10L)).thenReturn(List.of("spring"));
        }
    }

    @Nested
    class Lists {

        @Test
        void categoryAndQueryAreTurnedIntoFilters() {
            when(postRepository.findList(eq(PostStatus.ACTIVE), eq(EnumSet.of(PostCategory.GAME)), eq("%unity\\_3d%"), eq(null), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(post(AUTHOR, 0)), PageRequest.of(0, 20), 1));
            when(userService.getSummaries(List.of(AUTHOR))).thenReturn(Map.of(AUTHOR, author()));

            var page = service.getPosts("game", " Unity_3D ", "latest", null, PageRequest.of(0, 20));

            // 분류·검색어가 위 조건(GAME, "%unity\\_3d%")으로 넘어가야 결과가 나온다.
            assertThat(page.content()).hasSize(1);
            // 응답 분류는 소문자 코드로 나간다 (목 글은 BACKEND).
            assertThat(page.content().get(0).category()).isEqualTo("backend");
        }

        @Test
        void noCategoryMeansAllCategories() {
            when(postRepository.findList(eq(PostStatus.ACTIVE), eq(EnumSet.allOf(PostCategory.class)), eq("%"), eq(null), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
            when(userService.getSummaries(List.of())).thenReturn(Map.of());

            assertThat(service.getPosts(null, null, "popular", null, PageRequest.of(0, 20)).content()).isEmpty();
        }
    }

    private static WritePostRequest request(String category, List<String> tags, int price) {
        return new WritePostRequest("제목", category, tags, BODY, null, price);
    }

    private static Post post(Long userId, int price) {
        Post post = Post.builder().userId(userId).title("제목").category(PostCategory.BACKEND)
                .content(BODY).paid(price > 0).priceChips(price).build();
        return withId(post, 10L);
    }

    private static Tag tag(Long id, String name) {
        return withId(Tag.builder().name(name).build(), id);
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    private static UserSummaryResponse author() {
        return new UserSummaryResponse(AUTHOR, "작성자", null, List.of());
    }
}
