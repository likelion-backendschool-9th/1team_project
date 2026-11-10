package com.dipsource.service.user;

/** 딥소스 플러스. 시작은 결제 없이 기간만 열고, 만료 뒤 자동 갱신만 가상 결제한다. 블로그 구독과는 별개다. */
public interface PlusSubscriptionService {

    /**
     * 플러스를 시작한다. 이미 플러스면 만료 시각은 유지하고 자동 갱신만 켠다.
     * 없는 회원은 404.
     */
    void start(Long userId);

    /** 자동 갱신을 끈다. 만료 시각까지는 플러스가 유지된다. 없는 회원은 404. */
    void cancel(Long userId);

    /**
     * 만료됐고 자동 갱신이 켜진 회원을 30일씩 연장한다. 한 번 연장할 때마다 가상 결제 한 건을 남긴다.
     * 서버가 꺼져 있던 기간은 30일 단위로 따라잡는다.
     */
    void renewDue();
}
