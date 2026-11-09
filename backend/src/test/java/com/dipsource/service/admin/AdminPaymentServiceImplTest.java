package com.dipsource.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.admin.AdminCoinTxResponse;
import com.dipsource.dto.admin.AdminRevenueResponse;
import com.dipsource.dto.admin.AdminSubscriptionResponse;
import com.dipsource.entity.coin.Payment;
import com.dipsource.entity.coin.PaymentStatus;
import com.dipsource.entity.coin.PaymentType;
import com.dipsource.entity.post.BlogSubscription;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.coin.PaymentRepository;
import com.dipsource.repository.post.BlogSubscriptionRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.admin.impl.AdminPaymentServiceImpl;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminPaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BlogSubscriptionRepository blogSubscriptionRepository;
    @Mock private UserRepository userRepository;

    private AdminPaymentService service;

    @BeforeEach
    void setUp() {
        service = new AdminPaymentServiceImpl(paymentRepository, blogSubscriptionRepository, userRepository);
    }

    @Test
    void revenueFillsEmptyMonthsAndSplitsByType() {
        when(paymentRepository.sumPaidByMonth(
                LocalDateTime.of(2026, 9, 1, 0, 0), LocalDateTime.of(2026, 12, 1, 0, 0)))
                .thenReturn(List.of(
                        new Object[]{"2026-09", "CHIP_CHARGE", 4000L},
                        new Object[]{"2026-11", "CHIP_CHARGE", 10000L},
                        new Object[]{"2026-11", "BLOG_SUBSCRIPTION", 1500L}));

        List<AdminRevenueResponse> rows = service.revenue("2026-09", "2026-11");

        assertThat(rows).containsExactly(
                new AdminRevenueResponse("2026-09", 0, 0, 4000),
                new AdminRevenueResponse("2026-10", 0, 0, 0),
                new AdminRevenueResponse("2026-11", 1500, 0, 10000));
    }

    @Test
    void revenueOfMonthSumsEverything() {
        when(paymentRepository.sumPaidByMonth(any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{"2026-11", "CHIP_CHARGE", 10000L},
                new Object[]{"2026-11", "BLOG_SUBSCRIPTION", 1500L}));

        assertThat(service.revenueOf(YearMonth.of(2026, 11))).isEqualTo(11500);
    }

    @Test
    void revenueRejectsBadRange() {
        assertBadRequest(() -> service.revenue("2026-11", "2026-09"));
        assertBadRequest(() -> service.revenue("2024-01", "2026-11"));
        assertBadRequest(() -> service.revenue("11월", null));
    }

    @Test
    void chargesMapPaidAndRefundedWithNicknames() {
        Payment paid = payment(1L, 50, 10000, false);
        Payment refunded = payment(2L, 10, 2000, true);
        when(paymentRepository.findChargesForAdmin(eq(List.of(PaymentStatus.PAID, PaymentStatus.REFUNDED)), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(paid, refunded), PageRequest.of(0, 20), 2));
        when(userRepository.findAllById(List.of(7L))).thenReturn(List.of(user(7L, "충전러")));

        List<AdminCoinTxResponse> rows = service.listCharges(null, null, PageRequest.of(0, 20)).content();

        assertThat(rows).extracting(AdminCoinTxResponse::type, AdminCoinTxResponse::user, AdminCoinTxResponse::chips,
                        AdminCoinTxResponse::memo)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("charge", "충전러", 50, "10,000원 결제"),
                        org.assertj.core.groups.Tuple.tuple("refunded", "충전러", 10, "환불 완료"));
    }

    @Test
    void chargeTypeFilterAndUnknownType() {
        when(paymentRepository.findChargesForAdmin(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        service.listCharges(7L, "refunded", PageRequest.of(0, 20));

        verify(paymentRepository).findChargesForAdmin(eq(List.of(PaymentStatus.REFUNDED)), eq(7L), any());
        assertBadRequest(() -> service.listCharges(null, "refund-request", PageRequest.of(0, 20)));
    }

    @Test
    void canceledOrExpiredSubscriptionIsShownAsCanceled() {
        BlogSubscription active = subscription(1L, LocalDateTime.now().plusDays(10), false);
        BlogSubscription canceled = subscription(2L, LocalDateTime.now().plusDays(10), true);
        BlogSubscription expired = subscription(3L, LocalDateTime.now().minusDays(1), false);
        when(blogSubscriptionRepository.findAllByOrderByStartedAtDescIdDesc(any()))
                .thenReturn(new PageImpl<>(List.of(active, canceled, expired), PageRequest.of(0, 20), 3));
        when(userRepository.findAllById(List.of(21L, 22L))).thenReturn(List.of(user(21L, "구독자"), user(22L, "작가")));

        List<AdminSubscriptionResponse> rows = service.listSubscriptions(PageRequest.of(0, 20)).content();

        assertThat(rows).extracting(AdminSubscriptionResponse::status).containsExactly("active", "canceled", "canceled");
        assertThat(rows.getFirst().subscriber()).isEqualTo("구독자");
        assertThat(rows.getFirst().author()).isEqualTo("작가");
        assertThat(rows.getFirst().plan()).isEqualTo("blog");
    }

    private static Payment payment(Long id, int chips, int amountKrw, boolean refunded) {
        Payment payment = Payment.builder().userId(7L).paymentType(PaymentType.CHIP_CHARGE)
                .chips(chips).amountKrw(amountKrw).pgOrderId("LOCAL-" + id).build();
        ReflectionTestUtils.setField(payment, "id", id);
        ReflectionTestUtils.setField(payment, "createdAt", LocalDateTime.of(2026, 11, 9, 10, 0));
        payment.markPaid();
        if (refunded) {
            payment.refund(1L);
        }
        return payment;
    }

    private static BlogSubscription subscription(Long id, LocalDateTime expiresAt, boolean canceled) {
        BlogSubscription subscription = BlogSubscription.builder().subscriberId(21L).blogOwnerId(22L).paidPrice(500)
                .startedAt(LocalDateTime.now().minusDays(20)).expiresAt(expiresAt).build();
        ReflectionTestUtils.setField(subscription, "id", id);
        if (canceled) {
            subscription.cancel();
        }
        return subscription;
    }

    private static User user(Long id, String nickname) {
        User user = User.builder().email(id + "@test.local").nickname(nickname).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static void assertBadRequest(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }
}
