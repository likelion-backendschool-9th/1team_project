package com.dipsource.service.coin;

import com.dipsource.dto.coin.ChipHistoryResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 칩 충전·사용·거래 내역 서비스 */
public interface CoinService {

    /**
     * 패키지 번호로 칩을 충전함.
     * PG 연동 전에는 즉시 PAID 처리.
     * idempotencyKey가 있으면 중복 요청을 멱등하게 처리함 (추후 PG 주문 번호로 교체 예정)
     *
     * @param userId         충전하는 회원
     * @param packageId      패키지 번호 (1~4)
     * @param idempotencyKey 클라이언트 멱등 키 (선택)
     */
    void charge(Long userId, int packageId, String idempotencyKey);

    /**
     * 블로그 구독료 결제(현금). 칩은 쓰지 않고 payments에 BLOG_SUBSCRIPTION으로 남긴다.
     * PG 연동 전에는 충전처럼 즉시 PAID 처리한다.
     *
     * @param userId         구독하는 회원
     * @param subscriptionId blog_subscriptions.id
     * @param amountKrw      구독료(원)
     */
    void payBlogSubscription(Long userId, Long subscriptionId, int amountKrw);

    /**
     * 시리즈 구매 결제(현금). 칩은 쓰지 않고 payments에 SERIES_PURCHASE로 남긴다.
     * PG 연동 전에는 충전처럼 즉시 PAID 처리한다.
     *
     * @param userId    사는 회원
     * @param seriesId  post_series.id
     * @param amountKrw 시리즈 가격(원)
     */
    void paySeries(Long userId, Long seriesId, int amountKrw);

    /**
     * 한 회원의 칩 거래 내역을 최신순으로 반환한다.
     *
     * @param userId   조회 대상 회원
     * @param pageable 페이지 정보
     */
    PageResponse<ChipHistoryResponse> getHistory(Long userId, Pageable pageable);

    /**
     * 유료 글 결제. 구매자 칩을 차감하고 같은 칩을 글쓴이에게 판매 수익으로 넣는다.
     * 두 회원을 함께 잠그고, 거래 내역을 양쪽에 남긴다. 수수료는 정산 때 뗀다.
     * 잔액이 부족하면 InsufficientChipsException, 글쓴이가 정지·탈퇴했으면 404 (호출한 트랜잭션 전체가 롤백된다)
     *
     * @param buyerId   구매자
     * @param authorId  글쓴이
     * @param postId    글 (거래 내역의 ref_id)
     * @param postTitle 거래 내역 설명에 쓸 글 제목
     * @param chips     글 가격 (양수)
     */
    void payForPost(Long buyerId, Long authorId, Long postId, String postTitle, int chips);
}
