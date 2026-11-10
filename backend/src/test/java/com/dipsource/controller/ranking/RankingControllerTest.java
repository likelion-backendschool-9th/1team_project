package com.dipsource.controller.ranking;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.dto.ranking.RankingResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.global.exception.GlobalExceptionHandler;
import com.dipsource.service.ranking.RankingService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RankingControllerTest {

    @Mock
    private RankingService rankingService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new RankingController(rankingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 기본_조회는_데모_이번주이고_프론트_필드를_반환한다() throws Exception {
        when(rankingService.rank("demo", "weekly")).thenReturn(List.of(
                new RankingResponse(1, new UserSummaryResponse(3L, "시드팀", null, List.of("Java")), "채팅앱", 4L, 0)));

        mvc.perform(get("/rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[0].title").value("채팅앱"))
                .andExpect(jsonPath("$.data[0].score").value(4))
                .andExpect(jsonPath("$.data[0].change").value(0))
                .andExpect(jsonPath("$.data[0].user.id").value(3))
                .andExpect(jsonPath("$.data[0].user.nickname").value("시드팀"))
                .andExpect(jsonPath("$.data[0].user.techStacks[0]").value("Java"));
    }

    @Test
    void 잘못된_종류는_400이다() throws Exception {
        when(rankingService.rank("nope", "weekly"))
                .thenThrow(new BusinessException(ErrorCode.BAD_REQUEST, "순위 종류는 demo, blog, follower, home 중 하나예요."));

        mvc.perform(get("/rankings").param("type", "nope"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }
}
