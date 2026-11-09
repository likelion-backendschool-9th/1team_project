package com.dipsource.service.donation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.entity.coin.InsufficientChipsException;
import com.dipsource.entity.minihome.MiniHome;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.coin.ChipTransactionRepository;
import com.dipsource.repository.coin.DonationRepository;
import com.dipsource.repository.minihome.MiniHomeRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.donation.impl.DonationServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DonationServiceImplTest {

    private static final Long SENDER_ID = 1L;
    private static final Long RECEIVER_ID = 2L;

    @Mock private UserRepository userRepository;
    @Mock private MiniHomeRepository miniHomeRepository;
    @Mock private DonationRepository donationRepository;
    @Mock private ChipTransactionRepository chipTransactionRepository;

    private DonationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DonationServiceImpl(userRepository, miniHomeRepository, donationRepository, chipTransactionRepository);
    }

    private User activeUser(Long id, int balance) {
        User user = User.builder().email("test@test.com").nickname("user" + id).build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "chipBalance", balance);
        return user;
    }

    private User deletedUser(Long id) {
        User user = activeUser(id, 0);
        user.withdraw();
        return user;
    }

    private MiniHome miniHome(Long id, Long userId) {
        MiniHome mh = MiniHome.builder().userId(userId).build();
        ReflectionTestUtils.setField(mh, "id", id);
        return mh;
    }

    @Nested
    class Donate {

        @Test
        void 본인_미니홈피_후원_불가() {
            assertThatThrownBy(() -> service.donate(SENDER_ID, SENDER_ID, 10))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.BAD_REQUEST));

            verify(userRepository, never()).findByIdForUpdate(any());
        }

        @Test
        void 정지된_후원자_403() {
            User sender = activeUser(SENDER_ID, 100);
            sender.suspend();
            User receiver = activeUser(RECEIVER_ID, 0);

            // SENDER_ID(1) < RECEIVER_ID(2) 이므로 1번 먼저 잠금
            when(userRepository.findByIdForUpdate(SENDER_ID)).thenReturn(Optional.of(sender));
            when(userRepository.findByIdForUpdate(RECEIVER_ID)).thenReturn(Optional.of(receiver));

            assertThatThrownBy(() -> service.donate(SENDER_ID, RECEIVER_ID, 10))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.FORBIDDEN));

            verify(donationRepository, never()).save(any());
        }

        @Test
        void 탈퇴한_수신자_404() {
            User sender = activeUser(SENDER_ID, 100);
            User receiver = deletedUser(RECEIVER_ID);

            when(userRepository.findByIdForUpdate(SENDER_ID)).thenReturn(Optional.of(sender));
            when(userRepository.findByIdForUpdate(RECEIVER_ID)).thenReturn(Optional.of(receiver));

            assertThatThrownBy(() -> service.donate(SENDER_ID, RECEIVER_ID, 10))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.NOT_FOUND));

            verify(donationRepository, never()).save(any());
        }

        @Test
        void 정지된_수신자_404() {
            User sender = activeUser(SENDER_ID, 100);
            User receiver = activeUser(RECEIVER_ID, 0);
            receiver.suspend();

            when(userRepository.findByIdForUpdate(SENDER_ID)).thenReturn(Optional.of(sender));
            when(userRepository.findByIdForUpdate(RECEIVER_ID)).thenReturn(Optional.of(receiver));

            assertThatThrownBy(() -> service.donate(SENDER_ID, RECEIVER_ID, 10))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.NOT_FOUND));

            verify(donationRepository, never()).save(any());
        }

        @Test
        void 잔액_부족하면_예외() {
            User sender = activeUser(SENDER_ID, 5);
            User receiver = activeUser(RECEIVER_ID, 0);
            MiniHome mh = miniHome(10L, RECEIVER_ID);

            when(userRepository.findByIdForUpdate(SENDER_ID)).thenReturn(Optional.of(sender));
            when(userRepository.findByIdForUpdate(RECEIVER_ID)).thenReturn(Optional.of(receiver));
            when(miniHomeRepository.findByUserId(RECEIVER_ID)).thenReturn(Optional.of(mh));

            assertThatThrownBy(() -> service.donate(SENDER_ID, RECEIVER_ID, 10))
                    .isInstanceOf(InsufficientChipsException.class);

            verify(donationRepository, never()).save(any());
        }

        @Test
        void 후원_성공하면_잔액_이동() {
            User sender = activeUser(SENDER_ID, 50);
            User receiver = activeUser(RECEIVER_ID, 10);
            MiniHome mh = miniHome(10L, RECEIVER_ID);

            when(userRepository.findByIdForUpdate(SENDER_ID)).thenReturn(Optional.of(sender));
            when(userRepository.findByIdForUpdate(RECEIVER_ID)).thenReturn(Optional.of(receiver));
            when(miniHomeRepository.findByUserId(RECEIVER_ID)).thenReturn(Optional.of(mh));
            when(donationRepository.save(any())).thenAnswer(inv -> {
                var d = inv.getArgument(0, com.dipsource.entity.coin.Donation.class);
                ReflectionTestUtils.setField(d, "id", 99L);
                return d;
            });

            service.donate(SENDER_ID, RECEIVER_ID, 20);

            assertThat(sender.getChipBalance()).isEqualTo(30);
            assertThat(receiver.getChipBalance()).isEqualTo(30);
            verify(chipTransactionRepository).save(
                    org.mockito.ArgumentMatchers.argThat(tx -> tx.getAmount() == -20));
            verify(chipTransactionRepository).save(
                    org.mockito.ArgumentMatchers.argThat(tx -> tx.getAmount() == 20));
        }
    }
}
