package com.dipsource.service.settlement.impl;

import com.dipsource.dto.settlement.SettlementGenerateResponse;
import com.dipsource.dto.settlement.SettlementResponse;
import com.dipsource.entity.admin.AdminActionLog;
import com.dipsource.entity.coin.ChipRefType;
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
import com.dipsource.service.settlement.SettlementService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link SettlementService} 구현체. */
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    /** 칩 1개 금액(원). 프론트 policy.ts의 CHIP_PRICE_WON, CoinServiceImpl과 같다 */
    static final int CHIP_PRICE_WON = 200;

    /** 정산 대상이 되는 수익 거래. 구독 수익 종류가 생기면 여기에 추가한다 */
    static final List<ChipTransactionType> INCOME_TYPES =
            List.of(ChipTransactionType.BLOG_INCOME, ChipTransactionType.DONATION_INCOME);

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final SettlementRepository settlementRepository;
    private final ChipTransactionRepository chipTransactionRepository;
    private final UserRepository userRepository;
    private final AdminActionLogRepository adminActionLogRepository;

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
        if (period.isAfter(YearMonth.now(SEOUL))) {
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
            int chips = Math.toIntExact(sum.chips());
            int amount = chips * CHIP_PRICE_WON;
            settlementRepository.save(Settlement.builder()
                    .userId(sum.userId())
                    .totalChips(chips)
                    .amountKrw(amount)
                    .fee(amount * FEE_PERCENT / 100)
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
}
