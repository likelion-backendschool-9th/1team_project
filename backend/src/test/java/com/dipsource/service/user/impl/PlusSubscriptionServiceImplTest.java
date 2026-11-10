package com.dipsource.service.user.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.coin.CoinService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class PlusSubscriptionServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 10, 18, 0);
    private static final Clock CLOCK = Clock.fixed(NOW.atZone(ZoneId.of("Asia/Seoul")).toInstant(), ZoneId.of("Asia/Seoul"));

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoinService coinService;

    @Mock
    private TransactionTemplate transactionTemplate;

    private PlusSubscriptionServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new PlusSubscriptionServiceImpl(userRepository, coinService, transactionTemplate, CLOCK);
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
    void 만료되면_가상결제하고_30일_연장한다() {
        user.startPlus(NOW.minusDays(40));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        service.renewUser(1L, NOW);

        assertThat(user.getPlusUntil()).isEqualTo(NOW.minusDays(10).plusDays(30));
        assertThat(user.isPlus(NOW)).isTrue();
        assertThat(user.isPlusAutoRenew()).isTrue();
        verify(coinService).payPlus(1L, PlusSubscriptionServiceImpl.PRICE_KRW);
    }

    @Test
    void 여러_기간이_밀리면_기간마다_결제하고_미래가_될_때까지_연장한다() {
        user.startPlus(NOW.minusDays(70));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        service.renewUser(1L, NOW);

        assertThat(user.getPlusUntil()).isEqualTo(NOW.minusDays(40).plusDays(60));
        assertThat(user.isPlus(NOW)).isTrue();
        verify(coinService, times(2)).payPlus(1L, PlusSubscriptionServiceImpl.PRICE_KRW);
    }

    @Test
    void 아직_만료_전이거나_해지했으면_결제하지_않는다() {
        user.startPlus(NOW);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        service.renewUser(1L, NOW);

        user.cancelPlus();
        user.extendPlus();
        user.extendPlus();
        service.renewUser(1L, NOW.plusDays(90));

        verify(coinService, never()).payPlus(anyLong(), anyInt());
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
