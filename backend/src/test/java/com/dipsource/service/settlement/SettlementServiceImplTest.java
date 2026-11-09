package com.dipsource.service.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.settlement.SettlementGenerateResponse;
import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import com.dipsource.entity.settlement.Settlement;
import com.dipsource.entity.settlement.SettlementStatus;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.admin.AdminActionLogRepository;
import com.dipsource.repository.coin.ChipTransactionRepository;
import com.dipsource.repository.settlement.IncomeSum;
import com.dipsource.repository.settlement.SettlementRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.settlement.impl.SettlementServiceImpl;
import java.time.LocalDate;
import java.time.YearMonth;
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
class SettlementServiceImplTest {

    private static final Long ADMIN_ID = 99L;
    private static final YearMonth OCT = YearMonth.of(2026, 10);

    @Mock private SettlementRepository settlementRepository;
    @Mock private ChipTransactionRepository chipTransactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private AdminActionLogRepository adminActionLogRepository;

    private SettlementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SettlementServiceImpl(
                settlementRepository, chipTransactionRepository, userRepository, adminActionLogRepository);
    }

    @Test
    void generatesPendingSettlementWithThirtyPercentFee() {
        when(chipTransactionRepository.sumIncomeByUser(any(), any(), any()))
                .thenReturn(List.of(new IncomeSum(1L, 100L), new IncomeSum(2L, 50L)));
        when(settlementRepository.existsByUserIdAndPeriodStart(1L, LocalDate.of(2026, 10, 1))).thenReturn(false);
        when(settlementRepository.existsByUserIdAndPeriodStart(2L, LocalDate.of(2026, 10, 1))).thenReturn(true);
        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(user(ADMIN_ID, 0)));

        SettlementGenerateResponse result = service.generate(ADMIN_ID, OCT);

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(1);
        ArgumentCaptor<Settlement> captor = ArgumentCaptor.forClass(Settlement.class);
        verify(settlementRepository).save(captor.capture());
        Settlement saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getTotalChips()).isEqualTo(100);
        assertThat(saved.getAmountKrw()).isEqualTo(20_000);
        assertThat(saved.getFee()).isEqualTo(6_000);
        assertThat(saved.getNetAmount()).isEqualTo(14_000);
        assertThat(saved.getPeriodEnd()).isEqualTo(LocalDate.of(2026, 10, 31));
        assertThat(saved.getStatus()).isEqualTo(SettlementStatus.PENDING);
    }

    @Test
    void rejectsFutureMonth() {
        assertThatThrownBy(() -> service.generate(ADMIN_ID, YearMonth.now().plusMonths(2)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
        verify(settlementRepository, never()).save(any());
    }

    @Test
    void confirmDeductsChipsAndRecordsTransaction() {
        Settlement settlement = settlement(1L, 100);
        User creator = user(1L, 150);
        when(settlementRepository.findById(10L)).thenReturn(Optional.of(settlement));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(creator));
        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(user(ADMIN_ID, 0)));

        service.confirm(ADMIN_ID, 10L);

        assertThat(creator.getChipBalance()).isEqualTo(50);
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CONFIRMED);
        assertThat(settlement.getConfirmedBy()).isEqualTo(ADMIN_ID);
        ArgumentCaptor<ChipTransaction> tx = ArgumentCaptor.forClass(ChipTransaction.class);
        verify(chipTransactionRepository).save(tx.capture());
        assertThat(tx.getValue().getType()).isEqualTo(ChipTransactionType.SETTLEMENT);
        assertThat(tx.getValue().getAmount()).isEqualTo(-100);
        assertThat(tx.getValue().getBalanceAfter()).isEqualTo(50);
        verify(adminActionLogRepository, times(1)).save(any());
    }

    @Test
    void cannotConfirmTwice() {
        Settlement settlement = settlement(1L, 100);
        settlement.confirm(ADMIN_ID);
        when(settlementRepository.findById(10L)).thenReturn(Optional.of(settlement));

        assertThatThrownBy(() -> service.confirm(ADMIN_ID, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
        verify(chipTransactionRepository, never()).save(any());
    }

    @Test
    void rejectsWhenCreatorAlreadySpentChips() {
        Settlement settlement = settlement(1L, 100);
        User creator = user(1L, 30);
        when(settlementRepository.findById(10L)).thenReturn(Optional.of(settlement));
        when(userRepository.findByIdForUpdate(eq(1L))).thenReturn(Optional.of(creator));

        assertThatThrownBy(() -> service.confirm(ADMIN_ID, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(creator.getChipBalance()).isEqualTo(30);
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.PENDING);
    }

    private static Settlement settlement(Long userId, int chips) {
        int amount = chips * 200;
        Settlement settlement = Settlement.builder()
                .userId(userId)
                .totalChips(chips)
                .amountKrw(amount)
                .fee(amount * 30 / 100)
                .periodStart(LocalDate.of(2026, 10, 1))
                .periodEnd(LocalDate.of(2026, 10, 31))
                .build();
        ReflectionTestUtils.setField(settlement, "id", 10L);
        return settlement;
    }

    private static User user(Long id, int balance) {
        User user = User.builder().email("u" + id + "@test.local").nickname("회원" + id).build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "chipBalance", balance);
        return user;
    }
}
