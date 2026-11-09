package com.dipsource.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.admin.CreateSettlementsResponse;
import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import com.dipsource.entity.settlement.Settlement;
import com.dipsource.entity.settlement.SettlementStatus;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.admin.AdminActionLogRepository;
import com.dipsource.repository.coin.ChipTransactionRepository;
import com.dipsource.repository.settlement.SettlementRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.admin.impl.AdminSettlementServiceImpl;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
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
class AdminSettlementServiceImplTest {

    private static final Long ADMIN = 99L;
    private static final YearMonth LAST_MONTH = YearMonth.now(ZoneId.of("Asia/Seoul")).minusMonths(1);

    @Mock private SettlementRepository settlementRepository;
    @Mock private ChipTransactionRepository chipTransactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private AdminActionLogRepository adminActionLogRepository;

    private AdminSettlementService service;

    @BeforeEach
    void setUp() {
        service = new AdminSettlementServiceImpl(
                settlementRepository, chipTransactionRepository, userRepository, adminActionLogRepository);
    }

    @Test
    void createMakesOnePerCreatorWithThirtyPercentFeeAndSkipsExisting() {
        when(chipTransactionRepository.sumIncomeByUser(
                LAST_MONTH.atDay(1).atStartOfDay(), LAST_MONTH.plusMonths(1).atDay(1).atStartOfDay()))
                .thenReturn(List.of(new Object[]{22L, 30L}, new Object[]{1L, 10L}));
        when(settlementRepository.findUserIdsWithPeriod(eq(LAST_MONTH.atDay(1)), anyList())).thenReturn(List.of(1L));
        when(userRepository.findById(ADMIN)).thenReturn(Optional.of(user(ADMIN, 0)));
        when(settlementRepository.save(any())).thenAnswer(invocation -> withId(invocation.getArgument(0), 5L));

        CreateSettlementsResponse result = service.create(ADMIN, null);

        assertThat(result).isEqualTo(new CreateSettlementsResponse(LAST_MONTH.toString(), 1, 1));
        ArgumentCaptor<Settlement> saved = ArgumentCaptor.forClass(Settlement.class);
        verify(settlementRepository).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(22L);
        assertThat(saved.getValue().getAmountKrw()).isEqualTo(6000);
        assertThat(saved.getValue().getFee()).isEqualTo(1800);
        assertThat(saved.getValue().getNetAmount()).isEqualTo(4200);
        assertThat(saved.getValue().getPeriodEnd()).isEqualTo(LAST_MONTH.atEndOfMonth());
        verify(adminActionLogRepository).save(any());
    }

    @Test
    void currentOrFutureMonthCannotBeSettled() {
        YearMonth now = YearMonth.now(ZoneId.of("Asia/Seoul"));
        assertBadRequest(() -> service.create(ADMIN, now.toString()));
        assertBadRequest(() -> service.create(ADMIN, now.plusMonths(1).toString()));
        assertBadRequest(() -> service.create(ADMIN, "지난달"));
    }

    @Test
    void confirmDeductsChipsAndRecordsSettlementTransaction() {
        Settlement settlement = settlement();
        User creator = user(22L, 50);
        when(settlementRepository.findById(5L)).thenReturn(Optional.of(settlement));
        when(userRepository.findByIdForUpdate(22L)).thenReturn(Optional.of(creator));
        when(userRepository.findById(ADMIN)).thenReturn(Optional.of(user(ADMIN, 0)));

        service.confirm(ADMIN, 5L);

        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CONFIRMED);
        assertThat(settlement.getConfirmedBy()).isEqualTo(ADMIN);
        assertThat(creator.getChipBalance()).isEqualTo(20);
        ArgumentCaptor<ChipTransaction> tx = ArgumentCaptor.forClass(ChipTransaction.class);
        verify(chipTransactionRepository).save(tx.capture());
        assertThat(tx.getValue().getType()).isEqualTo(ChipTransactionType.SETTLEMENT);
        assertThat(tx.getValue().getAmount()).isEqualTo(-30);
        assertThat(tx.getValue().getBalanceAfter()).isEqualTo(20);
        assertThat(tx.getValue().getAdminId()).isEqualTo(ADMIN);
        verify(adminActionLogRepository).save(any());
    }

    @Test
    void confirmFailsWhenCreatorAlreadySpentChips() {
        Settlement settlement = settlement();
        when(settlementRepository.findById(5L)).thenReturn(Optional.of(settlement));
        when(userRepository.findByIdForUpdate(22L)).thenReturn(Optional.of(user(22L, 10)));

        assertBadRequest(() -> service.confirm(ADMIN, 5L));
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.PENDING);
        verify(chipTransactionRepository, never()).save(any());
    }

    @Test
    void confirmTwiceIsConflict() {
        Settlement settlement = settlement();
        settlement.confirm(ADMIN);
        when(settlementRepository.findById(5L)).thenReturn(Optional.of(settlement));

        assertThatThrownBy(() -> service.confirm(ADMIN, 5L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
        verify(userRepository, never()).findByIdForUpdate(any());
    }

    private static Settlement settlement() {
        return withId(Settlement.builder().userId(22L).totalChips(30).amountKrw(6000).fee(1800)
                .periodStart(LocalDate.of(2026, 10, 1)).periodEnd(LocalDate.of(2026, 10, 31)).build(), 5L);
    }

    private static User user(Long id, int chips) {
        User user = withId(User.builder().email(id + "@test.local").nickname("회원" + id).build(), id);
        ReflectionTestUtils.setField(user, "chipBalance", chips);
        return user;
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    private static void assertBadRequest(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }
}
