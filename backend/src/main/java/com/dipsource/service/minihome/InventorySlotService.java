package com.dipsource.service.minihome;

import com.dipsource.dto.minihome.InventorySlotResponse;
import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.coin.ChipTransactionRepository;
import com.dipsource.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 데모 인벤토리 칸 구매. 칩을 차감하고 칸을 한 칸 늘린다.
 * 잔액 변경과 칩 거래 내역 저장은 한 트랜잭션에서 한다 (User.deductChips 규칙).
 */
@Service
@RequiredArgsConstructor
public class InventorySlotService {

    /** 한 칸 가격(칩). 프론트 policy.ts의 INVENTORY_SLOT_CHIPS와 같아야 한다 */
    public static final int SLOT_PRICE_CHIPS = 10;

    /**
     * 칩으로 늘릴 수 있는 최대 칸 수.
     * 기본 6칸 + 14칸 = 20칸으로, 백엔드 데모 개수 한도(demo.quota.max-demos 기본 20)와 맞춘다.
     */
    public static final int MAX_EXTRA_SLOTS = 14;

    private final UserRepository userRepository;
    private final ChipTransactionRepository chipTransactionRepository;

    @Transactional
    public InventorySlotResponse buySlot(Long userId) {
        // 같은 회원이 버튼을 연달아 눌러도 잔액이 두 번 빠지지 않게 행을 잠근다
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "칩을 쓸 수 없는 계정이에요");
        }
        if (user.getExtraInventorySlots() >= MAX_EXTRA_SLOTS) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "인벤토리는 더 늘릴 수 없어요");
        }

        // 잔액이 부족하면 InsufficientChipsException (전역 핸들러가 안내 메시지로 바꾼다)
        user.deductChips(SLOT_PRICE_CHIPS);
        user.addInventorySlot();

        chipTransactionRepository.save(ChipTransaction.builder()
                .userId(userId)
                .type(ChipTransactionType.INVENTORY_SLOT)
                .amount(-SLOT_PRICE_CHIPS)
                .balanceAfter(user.getChipBalance())
                .description("데모 인벤토리 1칸 추가")
                .build());

        return new InventorySlotResponse(user.getExtraInventorySlots(), user.getChipBalance());
    }
}
