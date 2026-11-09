package com.dipsource.service.admin.impl;

import com.dipsource.dto.admin.AdminSettlementResponse;
import com.dipsource.dto.admin.CreateSettlementsResponse;
import com.dipsource.entity.admin.AdminActionLog;
import com.dipsource.entity.coin.ChipRefType;
import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import com.dipsource.entity.settlement.Settlement;
import com.dipsource.entity.settlement.SettlementStatus;
import com.dipsource.entity.user.User;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.admin.AdminActionLogRepository;
import com.dipsource.repository.coin.ChipTransactionRepository;
import com.dipsource.repository.settlement.SettlementRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.admin.AdminSettlementService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link AdminSettlementService} 구현체. */
@Service
@RequiredArgsConstructor
public class AdminSettlementServiceImpl implements AdminSettlementService {

    /** 칩 1개 금액(원). CoinServiceImpl.CHIP_PRICE_WON·프론트 policy.ts와 같다 */
    private static final int CHIP_PRICE_WON = 200;
    /** 정산 수수료율(%) — database-design.md "정산은 수수료 30%" */
    private static final int FEE_PERCENT = 30;
    private static final int MAX_PAGE_SIZE = 100;
    /** created_at은 서울 벽시계 시각으로 저장된다 (JpaAuditingConfig) */
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final SettlementRepository settlementRepository;
    private final ChipTransactionRepository chipTransactionRepository;
    private final UserRepository userRepository;
    private final AdminActionLogRepository adminActionLogRepository;

    @Override
    @Transactional
    public CreateSettlementsResponse create(Long adminId, String month) {
        YearMonth period = month == null || month.isBlank() ? YearMonth.now(ZONE).minusMonths(1) : parseMonth(month);
        if (!period.isBefore(YearMonth.now(ZONE))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "끝난 달만 정산할 수 있어요.");
        }
        LocalDate start = period.atDay(1);
        List<Object[]> incomes = chipTransactionRepository.sumIncomeByUser(
                start.atStartOfDay(), period.plusMonths(1).atDay(1).atStartOfDay());
        if (incomes.isEmpty()) {
            return new CreateSettlementsResponse(period.toString(), 0, 0);
        }

        Set<Long> already = new HashSet<>(settlementRepository.findUserIdsWithPeriod(
                start, incomes.stream().map(row -> (Long) row[0]).toList()));
        User admin = findAdmin(adminId);
        int created = 0;
        for (Object[] row : incomes) {
            Long userId = (Long) row[0];
            int chips = ((Number) row[1]).intValue();
            if (chips <= 0 || already.contains(userId)) {
                continue;
            }
            int amount = chips * CHIP_PRICE_WON;
            Settlement saved = settlementRepository.save(Settlement.builder()
                    .userId(userId)
                    .totalChips(chips)
                    .amountKrw(amount)
                    .fee(amount * FEE_PERCENT / 100)
                    .periodStart(start)
                    .periodEnd(period.atEndOfMonth())
                    .build());
            log(admin, "SETTLEMENT_CREATE", saved.getId());
            created++;
        }
        return new CreateSettlementsResponse(period.toString(), created, incomes.size() - created);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminSettlementResponse> list(String status, String month, Pageable pageable) {
        LocalDate periodStart = month == null || month.isBlank() ? null : parseMonth(month).atDay(1);
        Page<Settlement> page = settlementRepository.findForAdmin(parseStatus(status), periodStart, limited(pageable));
        Map<Long, String> nicknames = nicknamesOf(page.getContent().stream().map(Settlement::getUserId).toList());
        return PageResponse.of(page, s -> AdminSettlementResponse.of(s, nicknames.getOrDefault(s.getUserId(), "삭제된 유저")));
    }

    @Override
    @Transactional
    public void confirm(Long adminId, Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "정산을 찾을 수 없어요."));
        if (settlement.getStatus() != SettlementStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 확정한 정산이에요.");
        }
        // 작가를 잠근 뒤 칩을 뺀다. 현금으로 지급할 칩이라 잔액에서 빠져야 같은 칩을 다시 쓰지 못한다
        User creator = userRepository.findByIdForUpdate(settlement.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "작가를 찾을 수 없어요."));
        if (creator.getChipBalance() < settlement.getTotalChips()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "작가의 칩(" + creator.getChipBalance() + "개)이 정산할 칩(" + settlement.getTotalChips()
                            + "개)보다 적어요. 이미 사용한 칩은 정산할 수 없어요.");
        }
        creator.deductChips(settlement.getTotalChips());
        chipTransactionRepository.save(ChipTransaction.builder()
                .userId(creator.getId())
                .type(ChipTransactionType.SETTLEMENT)
                .amount(-settlement.getTotalChips())
                .balanceAfter(creator.getChipBalance())
                .refType(ChipRefType.SETTLEMENT)
                .refId(settlement.getId())
                .description(settlement.getPeriodStart().getMonthValue() + "월 수익 정산")
                .adminId(adminId)
                .build());
        settlement.confirm(adminId);
        log(findAdmin(adminId), "SETTLEMENT_CONFIRM", settlement.getId());
    }

    private User findAdmin(Long adminId) {
        return userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "관리자를 찾을 수 없어요."));
    }

    private void log(User admin, String actionType, Long targetId) {
        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(admin)
                .actionType(actionType)
                .targetType("SETTLEMENT")
                .targetId(targetId)
                .build());
    }

    private static SettlementStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return switch (status) {
            case "pending" -> SettlementStatus.PENDING;
            case "done" -> SettlementStatus.CONFIRMED;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "상태는 pending 또는 done이에요.");
        };
    }

    private static YearMonth parseMonth(String value) {
        try {
            return YearMonth.parse(value.strip());
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "월은 YYYY-MM 형식으로 입력해 주세요.");
        }
    }

    private Map<Long, String> nicknamesOf(List<Long> userIds) {
        List<Long> distinct = userIds.stream().distinct().toList();
        if (distinct.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(distinct).stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayNickname));
    }

    private static Pageable limited(Pageable pageable) {
        int size = pageable.getPageSize() <= 0 ? 20 : Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        return PageRequest.of(pageable.getPageNumber(), size);
    }
}
