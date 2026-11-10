package com.dipsource.service.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.settlement.MyDailyIncomeResponse;
import com.dipsource.dto.settlement.MyIncomeResponse;
import com.dipsource.dto.settlement.MyIncomeSourceResponse;
import com.dipsource.dto.settlement.SettlementGenerateResponse;
import com.dipsource.entity.coin.ChipRefType;
import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import com.dipsource.entity.coin.Donation;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostCategory;
import com.dipsource.entity.settlement.Settlement;
import com.dipsource.entity.settlement.SettlementStatus;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.admin.AdminActionLogRepository;
import com.dipsource.repository.coin.ChipTransactionRepository;
import com.dipsource.repository.coin.DonationRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.settlement.DailyIncome;
import com.dipsource.repository.settlement.IncomeSum;
import com.dipsource.repository.settlement.SettlementRepository;
import com.dipsource.repository.settlement.SourceIncome;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.settlement.impl.SettlementServiceImpl;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SettlementServiceImplTest {

    private static final Long ADMIN_ID = 99L;
    private static final YearMonth OCT = YearMonth.of(2026, 10);

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Mock private SettlementRepository settlementRepository;
    @Mock private ChipTransactionRepository chipTransactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private AdminActionLogRepository adminActionLogRepository;
    @Mock private PostRepository postRepository;
    @Mock private DonationRepository donationRepository;

    private SettlementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = newService(Clock.system(SEOUL));
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

    @Test
    void 내_정산만_조회한다() {
        Settlement mine = settlement(1L, 10);
        when(settlementRepository.findByUserIdOrderByPeriodStartDescIdDesc(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mine)));

        var page = service.mySettlements(1L, 0, 20);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).gross()).isEqualTo(2_000);
        assertThat(page.content().get(0).status()).isEqualTo(SettlementStatus.PENDING);
        verify(settlementRepository).findByUserIdOrderByPeriodStartDescIdDesc(eq(1L), any(Pageable.class));
        verify(settlementRepository, never()).findTop100ByOrderByPeriodStartDescIdDesc();
    }

    @Test
    void 정산이_없으면_빈_목록이고_예상_수익과_누적은_0이다() {
        SettlementServiceImpl timed = newService(clockAt(LocalDate.of(2026, 11, 11)));
        when(settlementRepository.findByUserIdOrderByPeriodStartDescIdDesc(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(settlementRepository.existsByUserIdAndPeriodStart(1L, LocalDate.of(2026, 11, 1))).thenReturn(false);
        when(chipTransactionRepository.sumIncomeForUser(eq(1L), any(), any(), any()))
                .thenReturn(new IncomeSum(1L, 0L));
        when(settlementRepository.sumNetAmountByUserIdAndStatus(1L, SettlementStatus.CONFIRMED)).thenReturn(0L);

        assertThat(timed.mySettlements(1L, 0, 20).content()).isEmpty();
        var summary = timed.mySummary(1L);
        assertThat(summary.expectedChips()).isZero();
        assertThat(summary.expectedGross()).isZero();
        assertThat(summary.expectedNet()).isZero();
        assertThat(summary.paidTotal()).isZero();
    }

    @Test
    void 이번_달_예상_수익은_정산_수수료와_같은_식이다() {
        SettlementServiceImpl timed = newService(clockAt(LocalDate.of(2026, 11, 11)));
        when(settlementRepository.existsByUserIdAndPeriodStart(1L, LocalDate.of(2026, 11, 1))).thenReturn(false);
        when(chipTransactionRepository.sumIncomeForUser(eq(1L), any(), any(), any()))
                .thenReturn(new IncomeSum(1L, 10L));
        when(settlementRepository.sumNetAmountByUserIdAndStatus(1L, SettlementStatus.CONFIRMED)).thenReturn(14_000L);

        var summary = timed.mySummary(1L);

        assertThat(summary.period()).isEqualTo("2026-11");
        assertThat(summary.expectedChips()).isEqualTo(10);
        assertThat(summary.expectedGross()).isEqualTo(2_000);
        assertThat(summary.expectedFee()).isEqualTo(600);
        assertThat(summary.expectedNet()).isEqualTo(1_400);
        assertThat(summary.paidTotal()).isEqualTo(14_000);
    }

    @Test
    void 이번_달_정산이_있으면_예상_수익은_0이다() {
        SettlementServiceImpl timed = newService(clockAt(LocalDate.of(2026, 11, 11)));
        when(settlementRepository.existsByUserIdAndPeriodStart(1L, LocalDate.of(2026, 11, 1))).thenReturn(true);
        when(settlementRepository.sumNetAmountByUserIdAndStatus(1L, SettlementStatus.CONFIRMED)).thenReturn(0L);

        var summary = timed.mySummary(1L);

        assertThat(summary.expectedGross()).isZero();
        verify(chipTransactionRepository, never()).sumIncomeForUser(any(), any(), any(), any());
    }

    @Test
    void 빈_날도_0으로_채우고_말일과_다음_달_1일_경계를_지킨다() {
        SettlementServiceImpl timed = newService(clockAt(LocalDate.of(2026, 11, 11)));
        ArgumentCaptor<LocalDateTime> from = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> to = ArgumentCaptor.forClass(LocalDateTime.class);
        when(chipTransactionRepository.sumIncomeByDay(eq(1L), any(), from.capture(), to.capture()))
                .thenReturn(List.of(
                        new DailyIncome(LocalDate.of(2026, 10, 1), 2L, 1L),
                        new DailyIncome(LocalDate.of(2026, 10, 31), 5L, 1L)));

        List<MyDailyIncomeResponse> rows = timed.myDaily(1L, YearMonth.of(2026, 10));

        assertThat(from.getValue()).isEqualTo(LocalDateTime.of(2026, 10, 1, 0, 0));
        assertThat(to.getValue()).isEqualTo(LocalDateTime.of(2026, 11, 1, 0, 0));
        assertThat(rows).hasSize(31);
        assertThat(rows.get(0).date()).isEqualTo("2026-10-01");
        assertThat(rows.get(0).chips()).isEqualTo(2);
        assertThat(rows.get(0).gross()).isEqualTo(400);
        assertThat(rows.get(1).chips()).isZero();
        assertThat(rows.get(1).count()).isZero();
        assertThat(rows.get(30).date()).isEqualTo("2026-10-31");
        assertThat(rows.get(30).chips()).isEqualTo(5);
    }

    @Test
    void 이번_달은_오늘까지만_채운다() {
        SettlementServiceImpl timed = newService(clockAt(LocalDate.of(2026, 11, 11)));
        ArgumentCaptor<LocalDateTime> to = ArgumentCaptor.forClass(LocalDateTime.class);
        when(chipTransactionRepository.sumIncomeByDay(eq(1L), any(), any(), to.capture()))
                .thenReturn(List.of());

        List<MyDailyIncomeResponse> rows = timed.myDaily(1L, YearMonth.of(2026, 11));

        assertThat(to.getValue()).isEqualTo(LocalDateTime.of(2026, 11, 12, 0, 0));
        assertThat(rows).hasSize(11);
        assertThat(rows.get(0).date()).isEqualTo("2026-11-01");
        assertThat(rows.get(10).date()).isEqualTo("2026-11-11");
    }

    @Test
    void 출처_합계는_없는_출처도_0으로_포함한다() {
        SettlementServiceImpl timed = newService(clockAt(LocalDate.of(2026, 11, 11)));
        when(chipTransactionRepository.sumIncomeBySource(eq(1L), any(), any(), any()))
                .thenReturn(List.of(new SourceIncome(ChipRefType.POST_PURCHASE, 10L, 2L)));

        List<MyIncomeSourceResponse> rows = timed.mySources(1L, YearMonth.of(2026, 10));

        assertThat(rows).extracting(MyIncomeSourceResponse::refType)
                .containsExactly(ChipRefType.POST_PURCHASE, ChipRefType.DONATION);
        assertThat(rows.get(0).label()).isEqualTo("글 판매");
        assertThat(rows.get(0).chips()).isEqualTo(10);
        assertThat(rows.get(0).gross()).isEqualTo(2_000);
        assertThat(rows.get(1).label()).isEqualTo("후원");
        assertThat(rows.get(1).chips()).isZero();
    }

    @Test
    void 수익_내역은_글_제목과_구매자_후원자_닉네임을_채운다() {
        SettlementServiceImpl timed = newService(clockAt(LocalDate.of(2026, 11, 11)));
        LocalDateTime at = LocalDateTime.of(2026, 10, 3, 9, 0);
        ChipTransaction postIncome = income(1L, ChipTransactionType.BLOG_INCOME, 10,
                ChipRefType.POST_PURCHASE, 50L, at);
        ChipTransaction donationIncome = income(1L, ChipTransactionType.DONATION_INCOME, 4,
                ChipRefType.DONATION, 8L, at.plusHours(1));
        ChipTransaction purchase = income(7L, ChipTransactionType.BLOG_PURCHASE, -10,
                ChipRefType.POST_PURCHASE, 50L, at.minusSeconds(1));
        Post post = Post.builder()
                .userId(1L).title("유료 글").category(PostCategory.BACKEND).content("본문").paid(true).priceChips(10)
                .build();
        ReflectionTestUtils.setField(post, "id", 50L);
        Donation donation = Donation.builder().senderId(9L).miniHomeId(3L).chips(4).build();
        ReflectionTestUtils.setField(donation, "id", 8L);
        when(chipTransactionRepository.findIncomeBetween(eq(1L), any(), any(), any()))
                .thenReturn(List.of(postIncome, donationIncome));
        when(postRepository.findAllById(any())).thenReturn(List.of(post));
        when(donationRepository.findAllById(any())).thenReturn(List.of(donation));
        when(chipTransactionRepository.findByTypeAndRefTypeAndRefIdIn(
                eq(ChipTransactionType.BLOG_PURCHASE), eq(ChipRefType.POST_PURCHASE), any()))
                .thenReturn(List.of(purchase));
        when(userRepository.findAllById(any())).thenReturn(List.of(user(7L, 0), user(9L, 0)));

        List<MyIncomeResponse> rows = timed.myIncomes(1L, YearMonth.of(2026, 10), null);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).label()).isEqualTo("글 판매");
        assertThat(rows.get(0).targetType()).isEqualTo("POST");
        assertThat(rows.get(0).targetId()).isEqualTo(50L);
        assertThat(rows.get(0).title()).isEqualTo("유료 글");
        assertThat(rows.get(0).senderNickname()).isEqualTo("회원7");
        assertThat(rows.get(0).chips()).isEqualTo(10);
        assertThat(rows.get(0).gross()).isEqualTo(2_000);
        assertThat(rows.get(1).label()).isEqualTo("후원");
        assertThat(rows.get(1).targetType()).isEqualTo("DONATION");
        assertThat(rows.get(1).title()).isEqualTo("미니홈피 후원");
        assertThat(rows.get(1).senderNickname()).isEqualTo("회원9");
        assertThat(rows.get(1).gross()).isEqualTo(800);
    }

    @Test
    void 월과_날짜를_같이_주거나_둘_다_없으면_400이다() {
        SettlementServiceImpl timed = newService(clockAt(LocalDate.of(2026, 11, 11)));
        assertThatThrownBy(() -> timed.myIncomes(1L, null, null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
        assertThatThrownBy(() -> timed.myIncomes(1L, YearMonth.of(2026, 10), LocalDate.of(2026, 10, 1)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    private SettlementServiceImpl newService(Clock clock) {
        return new SettlementServiceImpl(
                settlementRepository, chipTransactionRepository, userRepository, adminActionLogRepository,
                postRepository, donationRepository, clock);
    }

    private static Clock clockAt(LocalDate date) {
        return Clock.fixed(date.atStartOfDay(SEOUL).toInstant(), SEOUL);
    }

    private static ChipTransaction income(Long userId, ChipTransactionType type, int amount,
                                          ChipRefType refType, Long refId, LocalDateTime at) {
        ChipTransaction tx = ChipTransaction.builder()
                .userId(userId)
                .type(type)
                .amount(amount)
                .balanceAfter(0)
                .refType(refType)
                .refId(refId)
                .build();
        ReflectionTestUtils.setField(tx, "createdAt", at);
        return tx;
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
