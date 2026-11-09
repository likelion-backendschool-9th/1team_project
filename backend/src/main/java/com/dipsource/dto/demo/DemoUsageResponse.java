package com.dipsource.dto.demo;

/** 내 데모 사용량. usedBytes에는 업로드 중인 예약분까지 포함한다. */
public record DemoUsageResponse(
        long usedBytes,
        long limitBytes,
        long remainingBytes,
        int demoCount,
        int maxDemos
) {
}
