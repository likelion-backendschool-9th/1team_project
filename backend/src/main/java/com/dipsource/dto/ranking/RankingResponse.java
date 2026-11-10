package com.dipsource.dto.ranking;

import com.dipsource.dto.user.UserSummaryResponse;

/**
 * 랭킹 한 줄. 프론트 RankingRow와 같은 필드다.
 *
 * @param rank   1부터
 * @param user   작성자 또는 순위 대상 회원
 * @param title  데모·글 제목. 팔로워·미니홈피 순위는 빈 문자열
 * @param score  순위 점수
 * @param change 이전 순위와 차이. 스냅샷이 없어 항상 0
 */
public record RankingResponse(
        int rank,
        UserSummaryResponse user,
        String title,
        long score,
        int change
) {
}
