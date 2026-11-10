package com.dipsource.service.post;

import com.dipsource.dto.post.BlogSubscriptionInfoResponse;
import com.dipsource.dto.post.MySubscriptionResponse;
import java.util.List;

/** 블로그 구독. 구독료는 현금(원) 결제이고, 한 번 결제하면 한 달 동안 유료 글을 읽는다. */
public interface BlogSubscriptionService {

    /**
     * 구독. 한 달치를 결제한다. 자동 갱신은 PG 연동 후에 붙인다.
     * 이미 구독 중이면 아무것도 하지 않고, 해지했지만 아직 만료 전이면 결제 없이 다시 이어 준다 (멱등).
     * 나 자신·구독료가 0인 블로그는 400, 없는 회원·차단 관계는 404
     */
    void subscribe(Long subscriberId, Long blogOwnerId);

    /** 해지. 만료일까지는 계속 읽을 수 있다. 이미 해지했으면 그대로 둔다 (멱등). 내 구독이 아니면 404 */
    void cancel(Long subscriberId, Long subscriptionId);

    /** 내 구독 목록. 아직 만료 전인 것만 */
    List<MySubscriptionResponse> getMine(Long subscriberId);

    /** 블로그 주인 화면용 구독 정보. viewerId는 비로그인이면 null. 없는 회원·차단 관계는 404 */
    BlogSubscriptionInfoResponse getInfo(Long blogOwnerId, Long viewerId);

    /**
     * 내 블로그 월 구독료(원)를 바꾼다. 0이면 구독을 받지 않는다. 음수는 400.
     * 이미 결제한 구독은 결제한 가격 그대로 만료일까지 이어진다.
     */
    void changePrice(Long userId, int price);
}
