package com.dipsource.service.admin.impl;

import com.dipsource.dto.admin.AdminCoinTxResponse;
import com.dipsource.dto.admin.AdminRevenueResponse;
import com.dipsource.dto.admin.AdminSubscriptionResponse;
import com.dipsource.entity.coin.Payment;
import com.dipsource.entity.coin.PaymentStatus;
import com.dipsource.entity.coin.PaymentType;
import com.dipsource.entity.post.BlogSubscription;
import com.dipsource.entity.user.User;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.coin.PaymentRepository;
import com.dipsource.repository.post.BlogSubscriptionRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.admin.AdminPaymentService;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link AdminPaymentService} 구현체. */
@Service
@RequiredArgsConstructor
public class AdminPaymentServiceImpl implements AdminPaymentService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_MONTHS = 6;
    private static final int MAX_MONTHS = 24;
    /** created_at은 서울 벽시계 시각으로 저장된다 (JpaAuditingConfig) */
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final PaymentRepository paymentRepository;
    private final BlogSubscriptionRepository blogSubscriptionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminCoinTxResponse> listCharges(Long userId, String type, Pageable pageable) {
        Page<Payment> page = paymentRepository.findChargesForAdmin(statusesOf(type), userId, limited(pageable));
        Map<Long, String> nicknames = nicknamesOf(page.getContent().stream().map(Payment::getUserId).toList());
        return PageResponse.of(page, payment -> AdminCoinTxResponse.of(
                payment, nicknames.getOrDefault(payment.getUserId(), "삭제된 유저")));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminSubscriptionResponse> listSubscriptions(Pageable pageable) {
        Page<BlogSubscription> page = blogSubscriptionRepository.findAllByOrderByStartedAtDescIdDesc(limited(pageable));
        Map<Long, String> nicknames = nicknamesOf(page.getContent().stream()
                .flatMap(s -> Stream.of(s.getSubscriberId(), s.getBlogOwnerId()))
                .toList());
        LocalDateTime now = LocalDateTime.now(ZONE);
        return PageResponse.of(page, s -> AdminSubscriptionResponse.of(
                s,
                nicknames.getOrDefault(s.getSubscriberId(), "삭제된 유저"),
                nicknames.getOrDefault(s.getBlogOwnerId(), "삭제된 유저"),
                now));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRevenueResponse> revenue(String from, String to) {
        YearMonth end = to == null || to.isBlank() ? YearMonth.now(ZONE) : parseMonth(to);
        YearMonth start = from == null || from.isBlank() ? end.minusMonths(DEFAULT_MONTHS - 1) : parseMonth(from);
        if (start.isAfter(end)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "시작 월이 끝 월보다 늦어요.");
        }
        if (start.plusMonths(MAX_MONTHS).isBefore(end.plusMonths(1))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "최대 " + MAX_MONTHS + "개월까지 볼 수 있어요.");
        }

        // 달별로 [구독, 칩] 금액을 모은다. 결제가 없는 달도 0으로 채워서 돌려준다
        Map<String, long[]> sums = new HashMap<>();
        for (Object[] row : paymentRepository.sumPaidByMonth(
                start.atDay(1).atStartOfDay(), end.plusMonths(1).atDay(1).atStartOfDay())) {
            long[] sum = sums.computeIfAbsent((String) row[0], key -> new long[2]);
            long amount = ((Number) row[2]).longValue();
            // 시리즈 판매는 블로그 구독 자리(콘텐츠 매출)에 함께 더한다
            if (PaymentType.BLOG_SUBSCRIPTION.name().equals(row[1]) || PaymentType.SERIES_PURCHASE.name().equals(row[1])) {
                sum[0] += amount;
            } else if (PaymentType.CHIP_CHARGE.name().equals(row[1])) {
                sum[1] += amount;
            }
        }
        List<AdminRevenueResponse> result = new ArrayList<>();
        for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
            long[] sum = sums.getOrDefault(month.toString(), new long[2]);
            result.add(new AdminRevenueResponse(month.toString(), sum[0], 0, sum[1]));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public long revenueOf(YearMonth month) {
        AdminRevenueResponse row = revenue(month.toString(), month.toString()).getFirst();
        return row.subscription() + row.plus() + row.chips();
    }

    private static Collection<PaymentStatus> statusesOf(String type) {
        if (type == null || type.isBlank()) {
            return List.of(PaymentStatus.PAID, PaymentStatus.REFUNDED);
        }
        return switch (type) {
            case "charge" -> List.of(PaymentStatus.PAID);
            case "refunded" -> List.of(PaymentStatus.REFUNDED);
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "구분은 charge 또는 refunded예요.");
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
