package com.dipsource.service.minihome.impl;

import com.dipsource.service.minihome.MiniHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 서울 시간 자정에 오늘 방문자 수를 지운다. */
@Component
@RequiredArgsConstructor
public class MiniHomeVisitScheduler {

    private final MiniHomeService miniHomeService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void resetTodayCount() {
        miniHomeService.resetTodayCount();
    }
}
