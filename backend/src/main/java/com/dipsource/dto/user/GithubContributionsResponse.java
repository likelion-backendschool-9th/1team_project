package com.dipsource.dto.user;

import java.util.List;

/** 미니홈피 깃허브 잔디 응답. */
public record GithubContributionsResponse(
        long totalCommits,
        List<Day> days
) {
    /** date=YYYY-MM-DD, level=0~4 */
    public record Day(String date, int level) {
    }

    public static GithubContributionsResponse empty() {
        return new GithubContributionsResponse(0, List.of());
    }
}
