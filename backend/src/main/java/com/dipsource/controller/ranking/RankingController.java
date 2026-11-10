package com.dipsource.controller.ranking;

import com.dipsource.dto.ranking.RankingResponse;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.ranking.RankingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 데모·블로그·팔로워·미니홈피 순위. 비로그인도 볼 수 있다. */
@RestController
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/rankings")
    public ApiResponse<List<RankingResponse>> rankings(
            @RequestParam(defaultValue = "demo") String type,
            @RequestParam(defaultValue = "weekly") String period
    ) {
        return ApiResponse.of(rankingService.rank(type, period));
    }
}
