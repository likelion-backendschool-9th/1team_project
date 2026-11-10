package com.dipsource.dto.post;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 시리즈 화면. 시리즈 정보와 회차 목록.
 *
 * @param purchased 본문을 다 읽을 수 있는지 (작가 본인·구매자)
 * @param mine      내 시리즈인지
 * @param episodes  공개된 회차. 순서대로
 */
public record SeriesDetailResponse(
        SeriesSummaryResponse series,
        boolean purchased,
        boolean mine,
        List<Episode> episodes
) {

    /**
     * 회차 한 편.
     *
     * @param order 회차 번호(1부터). 지운 회차가 있으면 번호가 건너뛸 수 있다
     * @param paid  시리즈를 사야 읽을 수 있는 회차인지
     */
    public record Episode(Long postId, int order, String title, boolean paid, LocalDateTime createdAt) {
    }
}
