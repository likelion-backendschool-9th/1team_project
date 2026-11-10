package com.dipsource.service.donation.impl;

import com.dipsource.entity.coin.ChipRefType;
import com.dipsource.entity.coin.ChipTransaction;
import com.dipsource.entity.coin.ChipTransactionType;
import com.dipsource.entity.coin.Donation;
import com.dipsource.entity.notification.NotificationType;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.coin.ChipTransactionRepository;
import com.dipsource.repository.coin.DonationRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.donation.DonationService;
import com.dipsource.service.minihome.MiniHomeService;
import com.dipsource.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link DonationService} 구현체 */
@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final UserRepository userRepository;
    private final MiniHomeService miniHomeService;
    private final BlockRepository blockRepository;
    private final DonationRepository donationRepository;
    private final ChipTransactionRepository chipTransactionRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void donate(Long senderId, Long targetUserId, int chips) {
        // 본인 후원 불가
        if (senderId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "본인 미니홈피에는 후원할 수 없어요");
        }

        // 차단 관계면 404 — 방명록·팔로우·글 구매와 동일한 정책
        if (blockRepository.existsBetween(senderId, targetUserId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "미니홈피를 찾을 수 없어요");
        }

        // 데드락 방지: 항상 작은 ID를 먼저 잠금
        Long firstLockId = Math.min(senderId, targetUserId);
        Long secondLockId = Math.max(senderId, targetUserId);

        User firstUser = userRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요"));
        User secondUser = userRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요"));

        User sender = senderId.equals(firstLockId) ? firstUser : secondUser;
        User receiver = senderId.equals(firstLockId) ? secondUser : firstUser;

        // 후원자 상태 체크 — 락 안에서 체크해야 동시 요청에서도 안전함
        if (sender.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "후원할 수 없는 계정이에요");
        }

        // 정지·탈퇴한 미니홈피에는 후원 불가
        if (receiver.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "미니홈피를 찾을 수 없어요");
        }

        // 미니홈피 행이 없으면 기본값으로 생성 후 ID 반환
        Long miniHomeId = miniHomeService.getOrCreateMiniHomeId(targetUserId);

        // 잔액 차감 (부족하면 InsufficientChipsException)
        sender.deductChips(chips);
        receiver.addChips(chips);

        Donation donation = donationRepository.save(Donation.builder()
                .senderId(senderId)
                .miniHomeId(miniHomeId)
                .chips(chips)
                .build());

        // 송신자 거래 내역 (-칩)
        chipTransactionRepository.save(ChipTransaction.builder()
                .userId(senderId)
                .type(ChipTransactionType.DONATION)
                .amount(-chips)
                .balanceAfter(sender.getChipBalance())
                .refType(ChipRefType.DONATION)
                .refId(donation.getId())
                .description(receiver.getNickname() + "님 미니홈피에 칩 " + chips + "개 후원")
                .build());

        // 수신자 거래 내역 (+칩)
        chipTransactionRepository.save(ChipTransaction.builder()
                .userId(targetUserId)
                .type(ChipTransactionType.DONATION_INCOME)
                .amount(chips)
                .balanceAfter(receiver.getChipBalance())
                .refType(ChipRefType.DONATION)
                .refId(donation.getId())
                .description(sender.getNickname() + "님에게 칩 " + chips + "개 후원받음")
                .build());

        // 칩 이동과 내역 저장이 모두 끝난 뒤 알린다. 중간에 실패하면 여기까지 오지 않는다.
        notificationService.notify(targetUserId, senderId, NotificationType.SPONSOR,
                NotificationService.TARGET_MINI_HOME, targetUserId);
    }
}
