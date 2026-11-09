package com.dipsource.service.coin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.PaymentType;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.coin.ChipTransactionRepository;
import com.dipsource.repository.coin.PaymentRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.coin.impl.CoinServiceImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CoinServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock private UserRepository userRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ChipTransactionRepository chipTransactionRepository;

    private CoinServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CoinServiceImpl(userRepository, paymentRepository, chipTransactionRepository);
    }

    // 활성 유저 생성 헬퍼
    private User activeUser(int balance) {
        User user = User.builder().email("test@test.com").nickname("tester").build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        ReflectionTestUtils.setField(user, "chipBalance", balance);
        return user;
    }

    // 정지 유저 생성 헬퍼
    private User suspendedUser() {
        User user = activeUser(0);
        user.suspend();
        return user;
    }

    @Nested
    class Charge {

        @Test
        void 존재하지_않는_패키지_번호면_400() {
            User user = activeUser(0);
            when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(user));
            when(paymentRepository.existsByUserIdAndPaymentTypeAndCreatedAtAfter(
                    eq(USER_ID), eq(PaymentType.CHIP_CHARGE), any(LocalDateTime.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.charge(USER_ID, 99, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.BAD_REQUEST));

            verify(chipTransactionRepository, never()).save(any());
        }

        @Test
        void 정지된_회원이면_403() {
            when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(suspendedUser()));

            assertThatThrownBy(() -> service.charge(USER_ID, 1, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.FORBIDDEN));

            verify(paymentRepository, never()).save(any());
            verify(chipTransactionRepository, never()).save(any());
        }

        @Test
        void 쿨다운_안에_재요청하면_429() {
            when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(activeUser(0)));
            when(paymentRepository.existsByUserIdAndPaymentTypeAndCreatedAtAfter(
                    eq(USER_ID), eq(PaymentType.CHIP_CHARGE), any(LocalDateTime.class)))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.charge(USER_ID, 1, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.TOO_MANY_REQUESTS));

            verify(paymentRepository, never()).save(any());
            verify(chipTransactionRepository, never()).save(any());
        }

        @Test
        void 충전_성공하면_잔액이_늘어남() {
            int before = 50;
            User user = activeUser(before);
            when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(user));
            when(paymentRepository.existsByUserIdAndPaymentTypeAndCreatedAtAfter(
                    eq(USER_ID), eq(PaymentType.CHIP_CHARGE), any(LocalDateTime.class)))
                    .thenReturn(false);
            when(paymentRepository.save(any())).thenAnswer(inv -> {
                var p = inv.getArgument(0, com.dipsource.entity.coin.Payment.class);
                ReflectionTestUtils.setField(p, "id", 10L);
                p.markPaid();
                return p;
            });

            service.charge(USER_ID, 2, null); // 패키지 2 = 50칩

            assertThat(user.getChipBalance()).isEqualTo(before + 50);

            ArgumentCaptor<ChipTransaction> tx = ArgumentCaptor.forClass(ChipTransaction.class);
            verify(chipTransactionRepository).save(tx.capture());
            assertThat(tx.getValue().getAmount()).isEqualTo(50);
            assertThat(tx.getValue().getBalanceAfter()).isEqualTo(before + 50);
        }
    }
}
