package com.dipsource.dto.post;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.PostSeries;
import com.dipsource.entity.post.SeriesStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 시리즈 한 건 (프론트 PostSeries).
 *
 * @param authorTitle   블로그 이름
 * @param tags          시리즈 태그는 아직 없어서 빈 목록
 * @param status        "ongoing" / "completed"
 * @param episodeCount  공개된 회차 수
 * @param schedule      연재 주기는 아직 없어서 null
 * @param freeEpisodes  무료로 읽을 수 있는 회차 수
 * @param price         판매 가격(원). 0이면 무료 시리즈
 * @param firstPostId   1화 글 ID. 회차가 없으면 null
 * @param purchaseCount 구매한 사람 수
 */
public record SeriesSummaryResponse(
        Long id,
        UserSummaryResponse author,
        String authorTitle,
        String title,
        String description,
        List<String> tags,
        String status,
        int episodeCount,
        String schedule,
        int freeEpisodes,
        int price,
        Long firstPostId,
        long purchaseCount,
        LocalDateTime createdAt
) {

    /** 회차 통계. 회차가 없으면 EMPTY */
    public record Stats(int episodeCount, int freeEpisodes, Long firstPostId, long purchaseCount) {
        public static final Stats EMPTY = new Stats(0, 0, null, 0);
    }

    public static SeriesSummaryResponse of(PostSeries series, UserSummaryResponse author, Stats stats) {
        return new SeriesSummaryResponse(
                series.getId(),
                author,
                author.nickname() + "의 개발 블로그",
                series.getTitle(),
                series.getDescription(),
                List.of(),
                series.getStatus() == SeriesStatus.COMPLETED ? "completed" : "ongoing",
                stats.episodeCount(),
                null,
                stats.freeEpisodes(),
                series.getPriceKrw(),
                stats.firstPostId(),
                stats.purchaseCount(),
                series.getCreatedAt()
        );
    }
}
