package com.dipsource.service.block;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.global.exception.BusinessException;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.repository.post.PostBookmarkRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.block.impl.BlockServiceImpl;
import com.dipsource.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BlockServiceImplTest {

    private static final Long ME = 1L;
    private static final Long OTHER = 2L;

    @Mock private BlockRepository blockRepository;
    @Mock private FollowRepository followRepository;
    @Mock private PostBookmarkRepository postBookmarkRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;

    private BlockService service;

    @BeforeEach
    void setUp() {
        service = new BlockServiceImpl(blockRepository, followRepository, postBookmarkRepository, userRepository, userService);
    }

    @Test
    void blockRemovesFollowsAndBlogBookmarksBothWays() {
        when(userService.isActiveUser(OTHER)).thenReturn(true);

        service.block(ME, OTHER);

        verify(blockRepository).insertIgnore(ME, OTHER);
        verify(followRepository).deleteByFollowerIdAndFollowingId(ME, OTHER);
        verify(followRepository).deleteByFollowerIdAndFollowingId(OTHER, ME);
        verify(postBookmarkRepository).deleteBetween(ME, OTHER);
    }

    @Test
    void unblockDoesNotRestoreBookmarks() {
        service.unblock(ME, OTHER);

        verify(blockRepository).deleteByBlockerIdAndBlockedId(ME, OTHER);
        verify(postBookmarkRepository, never()).insertIgnore(anyLong(), anyLong());
    }

    @Test
    void cannotBlockInactiveUser() {
        when(userService.isActiveUser(OTHER)).thenReturn(false);

        assertThatThrownBy(() -> service.block(ME, OTHER)).isInstanceOf(BusinessException.class);
        verify(postBookmarkRepository, never()).deleteBetween(anyLong(), anyLong());
    }
}
