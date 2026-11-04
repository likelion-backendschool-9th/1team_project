package com.dipsource.dto.minihome;

/** 미니홈피 방문자 수. */
public record VisitorStatsResponse(
        int today,
        int total
) {
}
