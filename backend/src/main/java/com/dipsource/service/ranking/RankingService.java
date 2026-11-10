package com.dipsource.service.ranking;

import com.dipsource.dto.ranking.RankingResponse;
import java.util.List;

/** 데모·블로그·팔로워·미니홈피 순위. */
public interface RankingService {

    /**
     * 상위 20개.
     * type: demo, blog, follower, home. period: daily, weekly, monthly.
     */
    List<RankingResponse> rank(String type, String period);
}
