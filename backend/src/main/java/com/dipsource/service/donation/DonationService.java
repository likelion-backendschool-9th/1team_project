package com.dipsource.service.donation;

/** 후원 서비스 */
public interface DonationService {

    /**
     * 미니홈피에 칩을 후원함.
     *
     * @param senderId     후원하는 회원 ID
     * @param targetUserId 후원 받는 미니홈피 주인의 users.id
     * @param chips        후원할 칩 수 (1 이상)
     */
    void donate(Long senderId, Long targetUserId, int chips);
}
