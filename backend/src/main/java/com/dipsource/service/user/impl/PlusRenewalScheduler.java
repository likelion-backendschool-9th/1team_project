package com.dipsource.service.user.impl;

import com.dipsource.service.user.PlusSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 만료된 딥소스 플러스를 한 시간마다 가상 결제하고 30일 연장한다. */
@Component
@RequiredArgsConstructor
public class PlusRenewalScheduler {

    private final PlusSubscriptionService plusSubscriptionService;

    @Scheduled(cron = "0 5 * * * *", zone = "Asia/Seoul")
    public void renewExpiredPlus() {
        plusSubscriptionService.renewDue();
    }
}
