package com.dipsource.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.admin.AdminSuspiciousResponse;
import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import com.dipsource.entity.coin.Payment;
import com.dipsource.entity.coin.PaymentStatus;
import com.dipsource.entity.coin.PaymentType;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.admin.AdminActionLogRepository;
import com.dipsource.repository.coin.AdminCoinRepository;
import com.dipsource.repository.coin.ChipTransactionRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.admin.impl.AdminCoinServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminCoinServiceImplTest {

    private static final Long ADMIN = 99L;
    private static final Long USER = 25L;

    @Mock private AdminCoinRepository adminCoinRepository;
    @Mock private ChipTransactionRepository chipTransactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private AdminActionLogRepository adminActionLogRepository;

    private AdminCoinService service;

    @BeforeEach
    void setUp() {
        service = new AdminCoinServiceImpl(adminCoinRepository, chipTransactionRepository, userRepository, adminActionLogRepository);
    }

    @Test
    void refundTakesBackChipsAndMarksPaymentRefunded() {
        Payment payment = charge(50, PaymentStatus.PAID);
        User user = user(USER, 80);
        when(adminCoinRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(payment));
        when(userRepository.findByIdForUpdate(USER)).thenReturn(Optional.of(user));
        when(userRepository.findById(ADMIN)).thenReturn(Optional.of(user(ADMIN, 0)));

        service.refund(ADMIN, 3L);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getRefundedBy()).isEqualTo(ADMIN);
        assertThat(user.getChipBalance()).isEqualTo(30);
        ArgumentCaptor<ChipTransaction> tx = ArgumentCaptor.forClass(ChipTransaction.class);
        verify(chipTransactionRepository).save(tx.capture());
        assertThat(tx.getValue().getType()).isEqualTo(ChipTransactionType.REFUND);
        assertThat(tx.getValue().getAmount()).isEqualTo(-50);
        assertThat(tx.getValue().getBalanceAfter()).isEqualTo(30);
        verify(adminActionLogRepository).save(any());
    }

    @Test
    void refundFailsWhenChipsAlreadySpent() {
        Payment payment = charge(50, PaymentStatus.PAID);
        when(adminCoinRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(payment));
        when(userRepository.findByIdForUpdate(USER)).thenReturn(Optional.of(user(USER, 20)));

        assertError(() -> service.refund(ADMIN, 3L), ErrorCode.BAD_REQUEST);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        verify(chipTransactionRepository, never()).save(any());
    }

    @Test
    void refundTwiceIsConflictAndSubscriptionCannotBeRefunded() {
        when(adminCoinRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(charge(50, PaymentStatus.REFUNDED)));
        assertError(() -> service.refund(ADMIN, 3L), ErrorCode.CONFLICT);

        Payment subscription = Payment.builder().userId(USER).paymentType(PaymentType.BLOG_SUBSCRIPTION)
                .amountKrw(500).pgOrderId("LOCAL-sub").build();
        subscription.markPaid();
        when(adminCoinRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(subscription));
        assertError(() -> service.refund(ADMIN, 4L), ErrorCode.BAD_REQUEST);
        verify(userRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void suspiciousCombinesThreeRulesHighFirst() {
        LocalDateTime t1 = LocalDateTime.of(2026, 11, 9, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 11, 9, 12, 0);
        when(adminCoinRepository.findRapidChargers(any(), eq(5))).thenReturn(List.<Object[]>of(new Object[]{25L, 12L, 120000L, t1}));
        when(adminCoinRepository.findRepeatedRefunds(any(), eq(2))).thenReturn(List.<Object[]>of(new Object[]{24L, 2L, 4000L, t2}));
        when(adminCoinRepository.findRepeatedDonations(any(), anyInt())).thenReturn(List.<Object[]>of(new Object[]{21L, 22L, 6L, 60L, t2}));
        when(userRepository.findAllById(any())).thenReturn(List.of(
                user(21L, 0), user(22L, 0), user(24L, 0), user(25L, 0)));

        List<AdminSuspiciousResponse> rows = service.findSuspicious();

        assertThat(rows).hasSize(3);
        assertThat(rows.getFirst()).extracting(AdminSuspiciousResponse::userId, AdminSuspiciousResponse::level,
                        AdminSuspiciousResponse::pattern, AdminSuspiciousResponse::amount)
                .containsExactly(25L, "high", "24시간 안에 칩 충전 12회", 120000L);
        assertThat(rows).extracting(AdminSuspiciousResponse::level).containsExactly("high", "medium", "medium");
        assertThat(rows).extracting(AdminSuspiciousResponse::pattern)
                .contains("30일 안에 칩 충전 환불 2회", "7일 안에 회원22님에게 후원 6회");
        assertThat(rows).filteredOn(r -> r.userId().equals(21L)).first()
                .extracting(AdminSuspiciousResponse::amount).isEqualTo(12000L);
        assertThat(rows).extracting(AdminSuspiciousResponse::id).containsExactly(1L, 2L, 3L);
    }

    private static Payment charge(int chips, PaymentStatus status) {
        Payment payment = Payment.builder().userId(USER).paymentType(PaymentType.CHIP_CHARGE)
                .chips(chips).amountKrw(chips * 200).pgOrderId("LOCAL-" + chips).build();
        ReflectionTestUtils.setField(payment, "id", 3L);
        payment.markPaid();
        if (status == PaymentStatus.REFUNDED) {
            payment.refund(ADMIN);
        }
        return payment;
    }

    private static User user(Long id, int chips) {
        User user = User.builder().email(id + "@test.local").nickname("회원" + id).build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "chipBalance", chips);
        return user;
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ErrorCode code) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(code);
    }
}
