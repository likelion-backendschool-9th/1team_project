package com.dipsource.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 자정 방문 수 초기화처럼 주기 작업을 켠다. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
