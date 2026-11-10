package com.dipsource.service.minihome.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.entity.minihome.MiniHome;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.minihome.MiniHomeRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.post.PostService;
import com.dipsource.service.user.UserService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MiniHomeVisitServiceTest {

    @Mock
    private MiniHomeRepository repository;

    @Mock
    private UserService userService;

    @Mock
    private PostService postService;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private UserRepository userRepository;

    private MiniHomeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MiniHomeServiceImpl(
                new MiniHomeLoader(repository, userService), repository, postService, blockRepository, userRepository);
    }

    @Test
    void ownVisitDoesNotIncreaseCount() {
        when(userService.isActiveUser(1L)).thenReturn(true);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home(1L)));

        var response = service.recordVisit(1L, 1L);

        assertThat(response.today()).isZero();
        assertThat(response.total()).isZero();
        verify(repository, never()).incrementVisitCounts(1L);
    }

    @Test
    void anotherUsersVisitIncreasesCount() {
        when(userService.isActiveUser(2L)).thenReturn(true);
        when(repository.findByUserId(2L)).thenReturn(Optional.of(home(2L)));

        service.recordVisit(1L, 2L);

        verify(repository).incrementVisitCounts(2L);
    }

    @Test
    void surfPassesViewerAndExcludeToTheQuery() {
        when(repository.findRandomUserId(1L, 4L)).thenReturn(Optional.of(8L));

        var response = service.pickRandom(1L, 4L);

        assertThat(response.userId()).isEqualTo(8L);
        verify(repository).findRandomUserId(1L, 4L);
    }

    @Test
    void surfWithoutCandidateIsNotFound() {
        when(repository.findRandomUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pickRandom(1L, null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    private static MiniHome home(Long userId) {
        return MiniHome.builder().userId(userId).title("홈").build();
    }
}
