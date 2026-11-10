package com.dipsource.service.user;

/** 딥소스 플러스. 결제·칩 차감 없이 기간만 연다. 블로그 구독과는 별개다. */
public interface PlusSubscriptionService {

    /**
     * 플러스를 시작한다. 이미 플러스면 만료 시각은 유지하고 자동 갱신만 켠다.
     * 없는 회원은 404.
     */
    void start(Long userId);

    /** 자동 갱신을 끈다. 만료 시각까지는 플러스가 유지된다. 없는 회원은 404. */
    void cancel(Long userId);
}
