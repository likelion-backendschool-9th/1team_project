package com.dipsource.service.follow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.service.follow.impl.FollowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository repository;

    private FollowServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FollowServiceImpl(repository);
    }

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
