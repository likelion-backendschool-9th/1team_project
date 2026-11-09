package com.dipsource.service.admin.impl;

import com.dipsource.dto.admin.AdminSuspiciousResponse;
import com.dipsource.entity.admin.AdminActionLog;
import com.dipsource.entity.coin.ChipRefType;
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
import com.dipsource.service.admin.AdminCoinService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link AdminCoinService} 구현체. */
@Service
@RequiredArgsConstructor
public class AdminCoinServiceImpl implements AdminCoinService {

    /** 칩 1개 금액(원). CoinServiceImpl.CHIP_PRICE_WON·프론트 policy.ts와 같다 */
    private static final int CHIP_PRICE_WON = 200;
    /** created_at은 서울 벽시계 시각으로 저장된다 (JpaAuditingConfig) */
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    /** 규칙 1: 24시간 안에 충전 5회 이상 (10회 이상이면 high) */
    private static final int RAPID_CHARGE_HOURS = 24;
    private static final int RAPID_CHARGE_MIN = 5;
    private static final int RAPID_CHARGE_HIGH = 10;
    /** 규칙 2: 30일 안에 충전 환불 2회 이상 (3회 이상이면 high) */
    private static final int REFUND_DAYS = 30;
    private static final int REFUND_MIN = 2;
    private static final int REFUND_HIGH = 3;
    /** 규칙 3: 7일 안에 같은 사람에게 후원 5회 이상 (10회 이상이면 high) */
    private static final int DONATION_DAYS = 7;
    private static final int DONATION_MIN = 5;
    private static final int DONATION_HIGH = 10;

    private final AdminCoinRepository adminCoinRepository;
    private final ChipTransactionRepository chipTransactionRepository;
    private final UserRepository userRepository;
    private final AdminActionLogRepository adminActionLogRepository;

    @Override
    @Transactional
    public void refund(Long adminId, Long paymentId) {
        // 결제를 먼저 잠근다 — 두 관리자가 동시에 눌러도 두 번째는 REFUNDED를 보고 멈춘다
        Payment payment = adminCoinRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "결제를 찾을 수 없어요."));
        if (payment.getPaymentType() != PaymentType.CHIP_CHARGE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "칩 충전만 환불할 수 있어요.");
        }
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 환불한 결제예요.");
        }
        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "결제가 완료된 충전만 환불할 수 있어요.");
        }
        int chips = payment.getChips() == null ? 0 : payment.getChips();
        User user = userRepository.findByIdForUpdate(payment.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요."));
        if (user.getChipBalance() < chips) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "회원의 칩(" + user.getChipBalance() + "개)이 환불할 칩(" + chips
                            + "개)보다 적어요. 이미 사용한 칩은 환불할 수 없어요.");
        }

        user.deductChips(chips);
        chipTransactionRepository.save(ChipTransaction.builder()
                .userId(user.getId())
                .type(ChipTransactionType.REFUND)
                .amount(-chips)
                .balanceAfter(user.getChipBalance())
                .refType(ChipRefType.PAYMENT)
                .refId(payment.getId())
                .description("칩 " + chips + "개 충전 환불")
                .adminId(adminId)
                .build());
        payment.refund(adminId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "관리자를 찾을 수 없어요."));
        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(admin)
                .actionType("REFUND")
                .targetType("PAYMENT")
                .targetId(payment.getId())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminSuspiciousResponse> findSuspicious() {
        LocalDateTime now = LocalDateTime.now(ZONE);
        List<Object[]> rapid = adminCoinRepository.findRapidChargers(now.minusHours(RAPID_CHARGE_HOURS), RAPID_CHARGE_MIN);
        List<Object[]> refunds = adminCoinRepository.findRepeatedRefunds(now.minusDays(REFUND_DAYS), REFUND_MIN);
        List<Object[]> donations = adminCoinRepository.findRepeatedDonations(now.minusDays(DONATION_DAYS), DONATION_MIN);

        Set<Long> userIds = new HashSet<>();
        rapid.forEach(row -> userIds.add(toLong(row[0])));
        refunds.forEach(row -> userIds.add(toLong(row[0])));
        donations.forEach(row -> {
            userIds.add(toLong(row[0]));
            userIds.add(toLong(row[1]));
        });
        Map<Long, String> nicknames = userIds.isEmpty() ? Map.of() : userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayNickname));

        List<Draft> drafts = new ArrayList<>();
        for (Object[] row : rapid) {
            int count = toInt(row[1]);
            drafts.add(new Draft(toLong(row[0]), RAPID_CHARGE_HOURS + "시간 안에 칩 충전 " + count + "회",
                    count, toLong(row[2]), toTime(row[3]), count >= RAPID_CHARGE_HIGH));
        }
        for (Object[] row : refunds) {
            int count = toInt(row[1]);
            drafts.add(new Draft(toLong(row[0]), REFUND_DAYS + "일 안에 칩 충전 환불 " + count + "회",
                    count, toLong(row[2]), toTime(row[3]), count >= REFUND_HIGH));
        }
        for (Object[] row : donations) {
            int count = toInt(row[2]);
            String receiver = nicknames.getOrDefault(toLong(row[1]), "삭제된 유저");
            drafts.add(new Draft(toLong(row[0]), DONATION_DAYS + "일 안에 " + receiver + "님에게 후원 " + count + "회",
                    count, toLong(row[3]) * CHIP_PRICE_WON, toTime(row[4]), count >= DONATION_HIGH));
        }

        // 위험도 높은 것 → 최근 것 순. 번호는 화면 목록용이다
        drafts.sort(Comparator.comparing(Draft::high).reversed()
                .thenComparing(Draft::detectedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        List<AdminSuspiciousResponse> result = new ArrayList<>();
        for (int i = 0; i < drafts.size(); i++) {
            Draft d = drafts.get(i);
            result.add(new AdminSuspiciousResponse(
                    (long) i + 1,
                    d.userId(),
                    nicknames.getOrDefault(d.userId(), "삭제된 유저"),
                    d.pattern(),
                    d.count(),
                    d.amount(),
                    d.detectedAt() == null ? null : d.detectedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    d.high() ? "high" : "medium"));
        }
        return result;
    }

    private record Draft(Long userId, String pattern, int count, long amount, LocalDateTime detectedAt, boolean high) {
    }

    private static long toLong(Object value) {
        return ((Number) value).longValue();
    }

    private static int toInt(Object value) {
        return ((Number) value).intValue();
    }

    private static LocalDateTime toTime(Object value) {
        if (value instanceof LocalDateTime time) {
            return time;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }
}
