package com.dipsource.service.settlement.impl;

import com.dipsource.dto.settlement.MyDailyIncomeResponse;
import com.dipsource.dto.settlement.MyIncomeResponse;
import com.dipsource.dto.settlement.MyIncomeSourceResponse;
import com.dipsource.dto.settlement.MySettlementResponse;
import com.dipsource.dto.settlement.MySettlementSummaryResponse;
import com.dipsource.dto.settlement.SettlementGenerateResponse;
import com.dipsource.dto.settlement.SettlementResponse;
import com.dipsource.entity.admin.AdminActionLog;
import com.dipsource.entity.coin.ChipRefType;
import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import com.dipsource.entity.coin.Donation;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.settlement.Settlement;
import com.dipsource.entity.settlement.SettlementStatus;
import com.dipsource.entity.user.User;
import com.dipsource.global.common.PageResponse;
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
import com.dipsource.service.settlement.SettlementService;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link SettlementService} 구현체. */
@Service
public class SettlementServiceImpl implements SettlementService {

    /** 칩 1개 금액(원). 프론트 policy.ts의 CHIP_PRICE_WON, CoinServiceImpl과 같다 */
    static final int CHIP_PRICE_WON = 200;

    /** 한 페이지에 돌려줄 정산 수 상한 */
    static final int MAX_PAGE_SIZE = 20;

    /** 정산 대상이 되는 수익 거래. 구독 수익 종류가 생기면 여기에 추가한다 */
    static final List<ChipTransactionType> INCOME_TYPES =
            List.of(ChipTransactionType.BLOG_INCOME, ChipTransactionType.DONATION_INCOME);

    /** 수익이 없어도 출처 합계에 항상 보여 주는 종류 */
    private static final List<ChipRefType> SOURCE_ORDER =
            List.of(ChipRefType.POST_PURCHASE, ChipRefType.DONATION);

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final SettlementRepository settlementRepository;
    private final ChipTransactionRepository chipTransactionRepository;
    private final UserRepository userRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final PostRepository postRepository;
    private final DonationRepository donationRepository;
    private final Clock clock;

    @Autowired
    public SettlementServiceImpl(
            SettlementRepository settlementRepository,
            ChipTransactionRepository chipTransactionRepository,
            UserRepository userRepository,
            AdminActionLogRepository adminActionLogRepository,
            PostRepository postRepository,
            DonationRepository donationRepository
    ) {
        this(settlementRepository, chipTransactionRepository, userRepository, adminActionLogRepository,
                postRepository, donationRepository, Clock.system(SEOUL));
    }

    /** 테스트에서 오늘을 고정할 때 쓴다. */
    public SettlementServiceImpl(
            SettlementRepository settlementRepository,
            ChipTransactionRepository chipTransactionRepository,
            UserRepository userRepository,
            AdminActionLogRepository adminActionLogRepository,
            PostRepository postRepository,
            DonationRepository donationRepository,
            Clock clock
    ) {
        this.settlementRepository = settlementRepository;
        this.chipTransactionRepository = chipTransactionRepository;
        this.userRepository = userRepository;
        this.adminActionLogRepository = adminActionLogRepository;
        this.postRepository = postRepository;
        this.donationRepository = donationRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> list() {
        List<Settlement> rows = settlementRepository.findTop100ByOrderByPeriodStartDescIdDesc();
        Map<Long, String> names = nicknamesOf(rows.stream().map(Settlement::getUserId).toList());
        return rows.stream()
                .map(row -> SettlementResponse.of(row, names.getOrDefault(row.getUserId(), "삭제된 유저")))
                .toList();
    }

    @Override
    @Transactional
    public SettlementGenerateResponse generate(Long adminId, YearMonth period) {
        if (period.isAfter(YearMonth.now(clock))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "아직 오지 않은 달은 정산할 수 없어요.");
        }
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();
        List<IncomeSum> sums = chipTransactionRepository.sumIncomeByUser(
                INCOME_TYPES, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        int created = 0;
        int skipped = 0;
        for (IncomeSum sum : sums) {
            if (settlementRepository.existsByUserIdAndPeriodStart(sum.userId(), start)) {
                skipped++;
                continue;
            }
            Amounts amounts = Amounts.of(sum.chips());
            settlementRepository.save(Settlement.builder()
                    .userId(sum.userId())
                    .totalChips(amounts.chips())
                    .amountKrw(amounts.gross())
                    .fee(amounts.fee())
                    .periodStart(start)
                    .periodEnd(end)
                    .build());
            created++;
        }
        // 대상 ID 칸이 필수라 기간을 202610처럼 숫자로 남긴다
        log(adminId, "GENERATE_SETTLEMENT", (long) period.getYear() * 100 + period.getMonthValue(),
                period + " 생성 " + created + "건, 건너뜀 " + skipped + "건");
        return new SettlementGenerateResponse(period.toString(), created, skipped);
    }

    @Override
    @Transactional
    public void confirm(Long adminId, Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "정산을 찾을 수 없어요."));
        if (settlement.getStatus() != SettlementStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 확정한 정산이에요.");
        }
        // 같은 회원의 칩을 동시에 바꾸지 못하게 잠근 뒤 차감한다 (충전·구매·후원과 같은 방식)
        User user = userRepository.findByIdForUpdate(settlement.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요."));
        int chips = settlement.getTotalChips();
        if (user.getChipBalance() < chips) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "회원 칩(" + user.getChipBalance() + "개)이 정산 칩(" + chips + "개)보다 적어요. 수익 칩을 이미 사용했어요.");
        }
        user.deductChips(chips);
        chipTransactionRepository.save(ChipTransaction.builder()
                .userId(user.getId())
                .type(ChipTransactionType.SETTLEMENT)
                .amount(-chips)
                .balanceAfter(user.getChipBalance())
                .refType(ChipRefType.SETTLEMENT)
                .refId(settlement.getId())
                .description(settlement.getPeriodStart().toString().substring(0, 7)
                        + " 정산 (지급 " + settlement.getNetAmount() + "원)")
                .adminId(adminId)
                .build());
        settlement.confirm(adminId);
        log(adminId, "CONFIRM_SETTLEMENT", settlement.getId(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MySettlementResponse> mySettlements(Long userId, int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "페이지 크기는 1에서 20 사이예요.");
        }
        return PageResponse.of(
                settlementRepository.findByUserIdOrderByPeriodStartDescIdDesc(userId, PageRequest.of(page, size)),
                MySettlementResponse::of
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MySettlementSummaryResponse mySummary(Long userId) {
        YearMonth month = YearMonth.now(clock);
        LocalDate start = month.atDay(1);
        boolean alreadySettled = settlementRepository.existsByUserIdAndPeriodStart(userId, start);
        Amounts expected = alreadySettled
                ? Amounts.of(0)
                : Amounts.of(incomeChips(userId, start.atStartOfDay(), start.plusMonths(1).atStartOfDay()));
        int paid = Math.toIntExact(settlementRepository.sumNetAmountByUserIdAndStatus(
                userId, SettlementStatus.CONFIRMED));
        return new MySettlementSummaryResponse(
                month.toString(),
                expected.chips(),
                expected.gross(),
                expected.fee(),
                expected.net(),
                FEE_PERCENT,
                paid
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyDailyIncomeResponse> myDaily(Long userId, YearMonth month) {
        Window window = window(month);
        Map<LocalDate, DailyIncome> byDay = chipTransactionRepository
                .sumIncomeByDay(userId, INCOME_TYPES, window.from(), window.to())
                .stream()
                .collect(Collectors.toMap(DailyIncome::date, Function.identity(), (a, b) -> a));
        List<MyDailyIncomeResponse> rows = new ArrayList<>();
        for (LocalDate day = window.from().toLocalDate(); day.isBefore(window.to().toLocalDate()); day = day.plusDays(1)) {
            DailyIncome found = byDay.get(day);
            long chips = found == null || found.chips() == null ? 0 : found.chips();
            long count = found == null || found.count() == null ? 0 : found.count();
            Amounts amounts = Amounts.of(chips);
            rows.add(new MyDailyIncomeResponse(day.toString(), amounts.chips(), amounts.gross(), Math.toIntExact(count)));
        }
        return List.copyOf(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyIncomeSourceResponse> mySources(Long userId, YearMonth month) {
        Window window = window(month);
        Map<ChipRefType, SourceIncome> byType = new HashMap<>();
        for (SourceIncome row : chipTransactionRepository.sumIncomeBySource(
                userId, INCOME_TYPES, window.from(), window.to())) {
            byType.put(row.refType(), row);
        }
        List<ChipRefType> order = new ArrayList<>(SOURCE_ORDER);
        for (ChipRefType type : byType.keySet()) {
            if (type != null && !order.contains(type)) {
                order.add(type);
            }
        }
        List<MyIncomeSourceResponse> rows = new ArrayList<>();
        for (ChipRefType type : order) {
            rows.add(sourceRow(type, byType.get(type)));
        }
        if (byType.containsKey(null)) {
            rows.add(sourceRow(null, byType.get(null)));
        }
        return List.copyOf(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyIncomeResponse> myIncomes(Long userId, YearMonth month, LocalDate date) {
        if ((month == null) == (date == null)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "월 또는 날짜 중 하나만 골라 주세요.");
        }
        Window window = date == null ? window(month) : dayWindow(date);
        List<ChipTransaction> rows = chipTransactionRepository.findIncomeBetween(
                userId, INCOME_TYPES, window.from(), window.to());
        return describe(rows);
    }

    private List<MyIncomeResponse> describe(List<ChipTransaction> rows) {
        Set<Long> postIds = new HashSet<>();
        Set<Long> donationIds = new HashSet<>();
        for (ChipTransaction row : rows) {
            if (row.getRefId() == null) {
                continue;
            }
            if (row.getRefType() == ChipRefType.POST_PURCHASE) {
                postIds.add(row.getRefId());
            } else if (row.getRefType() == ChipRefType.DONATION) {
                donationIds.add(row.getRefId());
            }
        }
        Map<Long, Post> posts = postIds.isEmpty()
                ? Map.of()
                : postRepository.findAllById(postIds).stream()
                        .collect(Collectors.toMap(Post::getId, Function.identity(), (a, b) -> a));
        Map<Long, Donation> donations = donationIds.isEmpty()
                ? Map.of()
                : donationRepository.findAllById(donationIds).stream()
                        .collect(Collectors.toMap(Donation::getId, Function.identity(), (a, b) -> a));
        Map<Long, List<ChipTransaction>> buyers = postIds.isEmpty()
                ? Map.of()
                : chipTransactionRepository.findByTypeAndRefTypeAndRefIdIn(
                                ChipTransactionType.BLOG_PURCHASE, ChipRefType.POST_PURCHASE, postIds)
                        .stream()
                        .filter(tx -> tx.getRefId() != null)
                        .collect(Collectors.groupingBy(ChipTransaction::getRefId));
        Set<Long> senderIds = new HashSet<>();
        donations.values().forEach(donation -> senderIds.add(donation.getSenderId()));
        buyers.values().forEach(list -> list.forEach(tx -> senderIds.add(tx.getUserId())));
        Map<Long, String> names = nicknamesOf(List.copyOf(senderIds));

        return rows.stream().map(row -> toIncome(row, posts, donations, buyers, names)).toList();
    }

    private MyIncomeResponse toIncome(
            ChipTransaction row,
            Map<Long, Post> posts,
            Map<Long, Donation> donations,
            Map<Long, List<ChipTransaction>> buyers,
            Map<Long, String> names
    ) {
        Amounts amounts = Amounts.of(row.getAmount());
        ChipRefType refType = row.getRefType();
        String title = labelOf(refType);
        String sender = null;
        if (refType == ChipRefType.POST_PURCHASE) {
            Post post = row.getRefId() == null ? null : posts.get(row.getRefId());
            title = post == null ? "삭제된 글" : post.getTitle();
            ChipTransaction buyer = closestBuyer(buyers.get(row.getRefId()), row);
            if (buyer != null) {
                sender = names.get(buyer.getUserId());
            }
        } else if (refType == ChipRefType.DONATION) {
            title = "미니홈피 후원";
            Donation donation = row.getRefId() == null ? null : donations.get(row.getRefId());
            if (donation != null) {
                sender = names.get(donation.getSenderId());
            }
        }
        return new MyIncomeResponse(
                row.getCreatedAt(),
                refType,
                labelOf(refType),
                targetTypeOf(refType),
                row.getRefId(),
                title,
                sender,
                amounts.chips(),
                amounts.gross()
        );
    }

    /** 글 판매 수익에는 구매자 ID가 없다. 같은 글·같은 칩의 구매 거래 중 시각이 가장 가까운 사람을 고른다. */
    private static ChipTransaction closestBuyer(List<ChipTransaction> buyers, ChipTransaction income) {
        if (buyers == null || income.getCreatedAt() == null) {
            return null;
        }
        ChipTransaction best = null;
        long bestGap = Long.MAX_VALUE;
        for (ChipTransaction buyer : buyers) {
            if (buyer.getAmount() != -income.getAmount() || buyer.getCreatedAt() == null) {
                continue;
            }
            long gap = Math.abs(Duration.between(buyer.getCreatedAt(), income.getCreatedAt()).toNanos());
            if (gap < bestGap) {
                bestGap = gap;
                best = buyer;
            }
        }
        return best;
    }

    private MyIncomeSourceResponse sourceRow(ChipRefType type, SourceIncome found) {
        long chips = found == null || found.chips() == null ? 0 : found.chips();
        long count = found == null || found.count() == null ? 0 : found.count();
        Amounts amounts = Amounts.of(chips);
        return new MyIncomeSourceResponse(type, labelOf(type), amounts.chips(), amounts.gross(), Math.toIntExact(count));
    }

    private long incomeChips(Long userId, LocalDateTime from, LocalDateTime to) {
        IncomeSum sum = chipTransactionRepository.sumIncomeForUser(userId, INCOME_TYPES, from, to);
        if (sum == null || sum.chips() == null) {
            return 0;
        }
        return sum.chips();
    }

    /** 그 달 1일 0시부터 다음 달 1일 0시 전. 이번 달이면 내일 0시 전(오늘까지). */
    private Window window(YearMonth month) {
        YearMonth current = YearMonth.now(clock);
        if (month.isAfter(current)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "아직 오지 않은 달은 볼 수 없어요.");
        }
        LocalDateTime from = month.atDay(1).atStartOfDay();
        LocalDateTime to = month.equals(current)
                ? LocalDate.now(clock).plusDays(1).atStartOfDay()
                : month.plusMonths(1).atDay(1).atStartOfDay();
        return new Window(from, to);
    }

    private Window dayWindow(LocalDate date) {
        if (date.isAfter(LocalDate.now(clock))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "아직 오지 않은 날은 볼 수 없어요.");
        }
        return new Window(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    /**
     * ChipRefType → 화면 라벨.
     * POST_PURCHASE 글 판매, DONATION 후원, PAYMENT 결제, SETTLEMENT 정산.
     * 수익 거래는 글 판매·후원만 쌓인다.
     */
    static String labelOf(ChipRefType type) {
        if (type == null) {
            return "기타";
        }
        return switch (type) {
            case POST_PURCHASE -> "글 판매";
            case DONATION -> "후원";
            case PAYMENT -> "결제";
            case SETTLEMENT -> "정산";
        };
    }

    /** ChipRefType → 대상 종류. 글 판매의 refId는 글 ID라 대상은 POST다. */
    static String targetTypeOf(ChipRefType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case POST_PURCHASE -> "POST";
            case DONATION -> "DONATION";
            case PAYMENT -> "PAYMENT";
            case SETTLEMENT -> "SETTLEMENT";
        };
    }

    private void log(Long adminId, String action, Long targetId, String reason) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "관리자를 찾을 수 없습니다"));
        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(admin)
                .actionType(action)
                .targetType("SETTLEMENT")
                .targetId(targetId)
                .reason(reason)
                .build());
    }

    private Map<Long, String> nicknamesOf(List<Long> userIds) {
        List<Long> distinct = userIds.stream().distinct().toList();
        if (distinct.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(distinct).stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayNickname, (a, b) -> a));
    }

    /** 칩을 원·수수료·지급액으로 바꾼다. 관리자 정산 생성과 같은 정수 계산이다. */
    private record Amounts(int chips, int gross, int fee, int net) {

        static Amounts of(long chips) {
            int count = Math.toIntExact(chips);
            int gross = count * CHIP_PRICE_WON;
            int fee = gross * FEE_PERCENT / 100;
            return new Amounts(count, gross, fee, gross - fee);
        }
    }

    private record Window(LocalDateTime from, LocalDateTime to) {
    }
}
