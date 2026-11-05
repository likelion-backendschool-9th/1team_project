package com.dipsource.service.minihome.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.minihome.MiniHome;
import com.dipsource.entity.minihome.MiniHomeComment;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.minihome.MiniHomeCommentRepository;
import com.dipsource.service.follow.FollowService;
import com.dipsource.service.user.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MiniHomeCommentServiceImplTest {

    @Mock
    private MiniHomeLoader miniHomeLoader;

    @Mock
    private MiniHomeCommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private FollowService followService;

    private MiniHomeCommentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MiniHomeCommentServiceImpl(miniHomeLoader, commentRepository, userService, followService);
    }

    @Test
    void createsCommentWhenWriterHasNone() {
        MiniHome home = home(1L);
        when(miniHomeLoader.loadOrCreate(1L)).thenReturn(home);
        when(followService.isMutualFollow(2L, 1L)).thenReturn(true);
        when(commentRepository.findByHome_IdAndWriterId(10L, 2L)).thenReturn(Optional.empty());
        when(commentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getSummaries(any())).thenReturn(Map.of(
                2L, new UserSummaryResponse(2L, "글쓴이", null, List.of("Java"))
        ));

        var response = service.write(2L, 1L, "  안녕  ");

        assertThat(response.content()).isEqualTo("안녕");
        assertThat(response.secret()).isFalse();
        assertThat(response.author().nickname()).isEqualTo("글쓴이");
        assertThat(response.author().techStacks()).containsExactly("Java");
    }

    @Test
    void rewritesExistingCommentAndRestoresDeletedOne() {
        MiniHome home = home(1L);
        MiniHomeComment existing = MiniHomeComment.builder().home(home).writerId(2L).content("옛 글").build();
        existing.delete();
        LocalDateTime updatedAt = LocalDateTime.of(2026, 11, 5, 12, 0);
        ReflectionTestUtils.setField(existing, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
        ReflectionTestUtils.setField(existing, "updatedAt", updatedAt);
        when(miniHomeLoader.loadOrCreate(1L)).thenReturn(home);
        when(followService.isMutualFollow(2L, 1L)).thenReturn(true);
        when(commentRepository.findByHome_IdAndWriterId(10L, 2L)).thenReturn(Optional.of(existing));
        when(commentRepository.save(existing)).thenReturn(existing);
        when(userService.getSummaries(any())).thenReturn(Map.of(
                2L, new UserSummaryResponse(2L, "글쓴이", null, List.of())
        ));

        var response = service.write(2L, 1L, "새 글");

        assertThat(response.content()).isEqualTo("새 글");
        assertThat(response.createdAt()).isEqualTo(updatedAt);
        assertThat(existing.getContent()).isEqualTo("새 글");
        assertThat(existing.getDeletedAt()).isNull();
        verify(commentRepository).save(existing);
    }

    @Test
    void listWithoutHomeIsEmpty() {
        when(miniHomeLoader.findByUserId(1L)).thenReturn(Optional.empty());

        var response = service.list(1L, PageRequest.of(0, 20));

        assertThat(response.content()).isEmpty();
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isZero();
        verify(commentRepository, never()).findByHome_IdAndDeletedAtIsNull(any(), any());
    }

    @Test
    void ownerCannotWrite() {
        when(miniHomeLoader.loadOrCreate(1L)).thenReturn(home(1L));

        assertThatThrownBy(() -> service.write(1L, 1L, "내 글"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
        verify(followService, never()).isMutualFollow(any(), any());
    }

    @Test
    void rejectsWriterWhoIsNotMutual() {
        when(miniHomeLoader.loadOrCreate(1L)).thenReturn(home(1L));
        when(followService.isMutualFollow(2L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.write(2L, 1L, "안녕"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException business = (BusinessException) ex;
                    assertThat(business.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    assertThat(business.getMessage()).isEqualTo("맞팔한 사람만 일촌평을 쓸 수 있어요");
                });
    }

    @Test
    void onlyWriterOrOwnerCanDelete() {
        MiniHome home = home(1L);
        MiniHomeComment comment = MiniHomeComment.builder().home(home).writerId(2L).content("안녕").build();
        ReflectionTestUtils.setField(comment, "id", 5L);
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> service.delete(9L, 5L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
        assertThat(comment.getDeletedAt()).isNull();

        service.delete(2L, 5L);
        assertThat(comment.getDeletedAt()).isNotNull();

        MiniHomeComment another = MiniHomeComment.builder().home(home).writerId(2L).content("다른 글").build();
        when(commentRepository.findById(6L)).thenReturn(Optional.of(another));
        service.delete(1L, 6L);
        assertThat(another.getDeletedAt()).isNotNull();
    }

    private static MiniHome home(Long userId) {
        MiniHome home = MiniHome.builder().userId(userId).title("홈").build();
        ReflectionTestUtils.setField(home, "id", 10L);
        return home;
    }
}
