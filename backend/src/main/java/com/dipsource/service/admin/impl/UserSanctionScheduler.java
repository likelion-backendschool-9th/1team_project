package com.dipsource.service.admin.impl;

import com.dipsource.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 기간 제재가 끝나면 회원 상태를 ACTIVE로 되돌린다. */
@Component
@RequiredArgsConstructor
public class UserSanctionScheduler {

    private final AdminUserService adminUserService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void expireOverdueSanctions() {
        adminUserService.expireOverdueSanctions();
    }
}