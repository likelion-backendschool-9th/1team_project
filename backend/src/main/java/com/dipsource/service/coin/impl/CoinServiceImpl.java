package com.dipsource.service.coin.impl;

import com.dipsource.dto.coin.ChipHistoryResponse;
import com.dipsource.entity.coin.ChipRefType;
import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import com.dipsource.entity.coin.Payment;
import com.dipsource.entity.coin.PaymentStatus;
import com.dipsource.entity.coin.PaymentType;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.coin.ChipTransactionRepository;
import com.dipsource.repository.coin.PaymentRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.coin.CoinService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link CoinService} 구현체 */
@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {

    /**
     * 칩 패키지 목록. 패키지 번호 → 칩 수.
     * 프론트 mockPackages와 동일하게 맞춘다 (ID 1~4).
     */
    private static final Map<Integer, Integer> PACKAGES = Map.of(
            1, 10,
            2, 50,
            3, 100,
            4, 300
    );

    /** 칩 1개 금액(원). 프론트 policy.ts의 CHIP_PRICE_WON과 동일 */
    private static final int CHIP_PRICE_WON = 200;

    /** 같은 유저의 연속 충전 최소 간격(초). 연타 방지용 */
    private static final int CHARGE_COOLDOWN_SECONDS = 10;

    /** 거래 내역 한 번에 최대 조회 수. 프론트 HISTORY_PAGE_SIZE(20)보다 크게 유지할 것 */
    private static final int MAX_HISTORY_SIZE = 50;

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ChipTransactionRepository chipTransactionRepository;

    /** 서울 타임존. BaseEntity.createdAt 비교 기준을 명시적으로 맞춤 */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    @Transactional
    public void charge(Long userId, int packageId, String idempotencyKey) {
        // 유저 행을 먼저 잠근다 — 동시 요청이 들어와도 이 줄부터 직렬화됨
        // 쿨다운·멱등 체크를 락 안에서 해야 이중 충전을 막을 수 있음
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요."));

        // 정지·탈퇴 회원은 충전 불가. JWT가 살아있어도 거절함
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "충전할 수 없는 계정이에요.");
        }

        // [A] Cooldown 체크 — 락 안에서 체크해야 동시 요청 모두 차단됨
        // LocalDateTime.now(KST)로 BaseEntity.createdAt 기준(서울)과 타임존을 맞춤
        boolean tooSoon = paymentRepository.existsByUserIdAndPaymentTypeAndCreatedAtAfter(
                userId, PaymentType.CHIP_CHARGE,
                LocalDateTime.now(KST).minusSeconds(CHARGE_COOLDOWN_SECONDS));
        if (tooSoon) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS,
                    "잠시 후 다시 시도해 주세요. (" + CHARGE_COOLDOWN_SECONDS + "초 간격)");
        }

        // [B] 멱등 키 체크 — PG가 발급한 주문 번호로 중복 결제 방지
        // 쿨다운을 통과한 요청 중 이미 PAID 처리된 주문이면 조용히 반환
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            boolean alreadyPaid = paymentRepository.findByPgOrderId(idempotencyKey)
                    .map(p -> p.getStatus() == PaymentStatus.PAID)
                    .orElse(false);
            if (alreadyPaid) return;
        }

        int chips = resolvePackage(packageId);
        int amountKrw = chips * CHIP_PRICE_WON;

        // idempotencyKey가 있으면 pgOrderId로 쓴다. 없으면 임시 UUID 생성
        // PG 연동 전 즉시 PAID 처리 — 나중에 여기를 prepare/confirm으로 분리한다
        Payment payment = paymentRepository.save(Payment.builder()
                .userId(userId)
                .paymentType(PaymentType.CHIP_CHARGE)
                .amountKrw(amountKrw)
                .chips(chips)
                .pgOrderId(idempotencyKey != null && !idempotencyKey.isBlank()
                        ? idempotencyKey : generateOrderId())
                .build());

        // PG 검증 단계 — 현재는 no-op. 외부 PG 연동 시 여기서 PG API를 호출한다
        verifyPayment(payment);

        payment.markPaid();

        user.addChips(chips);

        chipTransactionRepository.save(ChipTransaction.builder()
                .userId(userId)
                .type(ChipTransactionType.CHARGE)
                .amount(chips)
                .balanceAfter(user.getChipBalance())
                .refType(ChipRefType.PAYMENT)
                .refId(payment.getId())
                .description("칩 " + chips + "개 충전")
                .build());
    }

    @Override
    @Transactional
    public void payBlogSubscription(Long userId, Long subscriptionId, int amountKrw) {
        if (amountKrw <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "결제할 금액이 없어요.");
        }
        Payment payment = paymentRepository.save(Payment.builder()
                .userId(userId)
                .paymentType(PaymentType.BLOG_SUBSCRIPTION)
                .subscriptionId(subscriptionId)
                .amountKrw(amountKrw)
                .pgOrderId(generateOrderId())
                .build());
        // 충전과 같은 PG 검증 자리. 지금은 no-op이라 바로 PAID 처리한다
        verifyPayment(payment);
        payment.markPaid();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChipHistoryResponse> getHistory(Long userId, Pageable pageable) {
        Pageable limited = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_HISTORY_SIZE)
        );
        return PageResponse.of(
                chipTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, limited),
                ChipHistoryResponse::of
        );
    }

    @Override
    @Transactional
    public void payForPost(Long buyerId, Long authorId, Long postId, String postTitle, int chips) {
        if (chips <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "결제할 칩이 없어요.");
        }
        // 두 회원을 항상 ID 순서로 잠근다 — 서로의 글을 동시에 사도 교착 상태가 생기지 않음
        User first = lockUser(Math.min(buyerId, authorId));
        User second = lockUser(Math.max(buyerId, authorId));
        User buyer = first.getId().equals(buyerId) ? first : second;
        User author = buyer == first ? second : first;

        if (buyer.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "칩을 사용할 수 없는 계정이에요.");
        }
        // 정지·탈퇴한 글쓴이에게는 수익을 쌓지 않는다 (후원과 같은 정책). 잠근 뒤에 확인해야 동시 정지에도 안전하다
        if (author.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "글을 찾을 수 없어요.");
        }

        buyer.deductChips(chips);
        author.addChips(chips);

        chipTransactionRepository.save(ChipTransaction.builder()
                .userId(buyer.getId())
                .type(ChipTransactionType.BLOG_PURCHASE)
                .amount(-chips)
                .balanceAfter(buyer.getChipBalance())
                .refType(ChipRefType.POST_PURCHASE)
                .refId(postId)
                .description("「" + postTitle + "」 잠금 해제")
                .build());
        chipTransactionRepository.save(ChipTransaction.builder()
                .userId(author.getId())
                .type(ChipTransactionType.BLOG_INCOME)
                .amount(chips)
                .balanceAfter(author.getChipBalance())
                .refType(ChipRefType.POST_PURCHASE)
                .refId(postId)
                .description("「" + postTitle + "」 판매 수익")
                .build());
    }

    private User lockUser(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요."));
    }

    /**
     * PG 결제 검증.
     * PG 연동 전에는 아무것도 하지 않는다 (no-op).
     * 외부 PG 연동 시 이 메서드에서 PG API를 호출해 승인 여부를 확인한다.
     */
    private void verifyPayment(Payment payment) {
        // TODO: PG 연동 후 PG API 호출 → 승인 실패 시 payment.cancel() 후 예외
    }

    /** 패키지 번호 → 칩 수 변환. 없는 패키지면 400 */
    private static int resolvePackage(int packageId) {
        Integer chips = PACKAGES.get(packageId);
        if (chips == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "존재하지 않는 패키지예요.");
        }
        return chips;
    }

    /** 임시 주문 번호 생성. PG 연동 후에는 PG가 발급한 주문 번호를 사용한다 */
    private static String generateOrderId() {
        return "LOCAL-" + UUID.randomUUID();
    }
}
