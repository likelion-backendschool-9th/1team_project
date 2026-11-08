package com.dipsource.service.minihome.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.minihome.Guestbook;
import com.dipsource.entity.minihome.MiniHome;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.minihome.GuestbookRepository;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GuestbookServiceImplTest {

    @Mock
    private MiniHomeLoader miniHomeLoader;

    @Mock
    private GuestbookRepository guestbookRepository;

    @Mock
    private UserService userService;

    @Mock
    private BlockRepository blockRepository;

    private GuestbookServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GuestbookServiceImpl(miniHomeLoader, guestbookRepository, userService, blockRepository);
    }

    @Test
    void hidesSecretContentFromOtherViewers() {
        MiniHome home = home(1L);
        Guestbook secret = Guestbook.builder().home(home).writerId(2L).content("비밀").secret(true).build();
        when(miniHomeLoader.findByUserId(1L)).thenReturn(Optional.of(home));
        when(guestbookRepository.findByHome_IdAndDeletedAtIsNull(any(), any()))
                .thenReturn(new PageImpl<>(List.of(secret)));
        when(userService.getSummaries(any())).thenReturn(Map.of(
                2L, new UserSummaryResponse(2L, "글쓴이", null, List.of())
        ));

        var hidden = service.list(3L, 1L, PageRequest.of(0, 20));
        var writer = service.list(2L, 1L, PageRequest.of(0, 20));
        var owner = service.list(1L, 1L, PageRequest.of(0, 20));

        assertThat(hidden.content().get(0).content()).isNull();
        assertThat(hidden.content().get(0).secret()).isTrue();
        assertThat(writer.content().get(0).content()).isEqualTo("비밀");
        assertThat(owner.content().get(0).content()).isEqualTo("비밀");
    }

    @Test
    void listWithoutHomeIsEmpty() {
        when(miniHomeLoader.findByUserId(1L)).thenReturn(Optional.empty());

        var response = service.list(3L, 1L, PageRequest.of(1, 20));

        assertThat(response.content()).isEmpty();
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isZero();
        verify(guestbookRepository, never()).findByHome_IdAndDeletedAtIsNull(any(), any());
    }

    @Test
    void onlyWriterOrOwnerCanDelete() {
        MiniHome home = home(1L);
        Guestbook entry = Guestbook.builder().home(home).writerId(2L).content("안녕").secret(false).build();
        when(guestbookRepository.findById(7L)).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> service.delete(9L, 7L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
        assertThat(entry.getDeletedAt()).isNull();

        service.delete(1L, 7L);
        assertThat(entry.getDeletedAt()).isNotNull();
    }

    private static MiniHome home(Long userId) {
        MiniHome home = MiniHome.builder().userId(userId).title("홈").build();
        ReflectionTestUtils.setField(home, "id", 10L);
        return home;
    }
}
