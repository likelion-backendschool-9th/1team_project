package com.dipsource.service.coin.impl;

import com.dipsource.dto.coin.ChipHistoryResponse;
import com.dipsource.entity.coin.ChipRefType;
import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import com.dipsource.entity.coin.Payment;
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

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ChipTransactionRepository chipTransactionRepository;

    @Override
    @Transactional
    public void charge(Long userId, int packageId) {
        int chips = resolvePackage(packageId);
        int amountKrw = chips * CHIP_PRICE_WON;

        // PG 연동 전 즉시 PAID 처리 — 나중에 여기를 prepare/confirm으로 분리한다
        Payment payment = paymentRepository.save(Payment.builder()
                .userId(userId)
                .paymentType(PaymentType.CHIP_CHARGE)
                .amountKrw(amountKrw)
                .chips(chips)
                .pgOrderId(generateOrderId())
                .build());

        // PG 검증 단계 — 현재는 no-op. 외부 PG 연동 시 여기서 PG API를 호출한다
        verifyPayment(payment);

        payment.markPaid();

        // 비관적 잠금으로 잔액 갱신 — 동시에 여러 충전이 들어와도 순서대로 처리된다
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요."));
        // 정지·탈퇴 회원은 충전 불가. JWT가 살아있어도 거절한다
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "충전할 수 없는 계정이에요.");
        }
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

    /** 거래 내역 한 번에 최대 조회 수 */
    private static final int MAX_HISTORY_SIZE = 50;

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
