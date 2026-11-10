package com.dipsource.service.user.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.user.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlusSubscriptionServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 10, 18, 0);
    private static final Clock CLOCK = Clock.fixed(NOW.atZone(ZoneId.of("Asia/Seoul")).toInstant(), ZoneId.of("Asia/Seoul"));

    @Mock
    private UserRepository userRepository;

    private PlusSubscriptionServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new PlusSubscriptionServiceImpl(userRepository, CLOCK);
        user = User.builder().email("me@test.local").nickname("회원").build();
    }

    @Test
    void 시작하면_플러스이고_30일_뒤가_만료다() {
        stubUser();

        service.start(1L);

        assertThat(user.isPlus(NOW)).isTrue();
        assertThat(user.isPlusAutoRenew()).isTrue();
        assertThat(user.getPlusUntil()).isEqualTo(NOW.plusDays(30));
    }

    @Test
    void 이미_플러스면_만료_시각은_그대로_두고_자동갱신만_켠다() {
        stubUser();
        user.startPlus(NOW.minusDays(10));
        LocalDateTime until = user.getPlusUntil();
        user.cancelPlus();

        service.start(1L);

        assertThat(user.getPlusUntil()).isEqualTo(until);
        assertThat(user.isPlusAutoRenew()).isTrue();
        assertThat(user.isPlus(NOW)).isTrue();
    }

    @Test
    void 해지해도_만료_전에는_플러스다() {
        stubUser();
        service.start(1L);

        service.cancel(1L);

        assertThat(user.isPlus(NOW)).isTrue();
        assertThat(user.isPlus(NOW.plusDays(30).minusSeconds(1))).isTrue();
        assertThat(user.isPlusAutoRenew()).isFalse();
        assertThat(user.getPlusUntil()).isEqualTo(NOW.plusDays(30));
    }

    @Test
    void 만료_시각이_지나면_플러스가_아니다() {
        stubUser();
        service.start(1L);

        assertThat(user.isPlus(NOW.plusDays(30))).isFalse();
    }

    @Test
    void 없는_회원이면_404이다() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.start(9L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    private void stubUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }
}
