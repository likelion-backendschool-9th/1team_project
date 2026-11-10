package com.dipsource.controller.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.dto.event.EventResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.global.exception.GlobalExceptionHandler;
import com.dipsource.service.event.EventService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new EventController(eventService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void 목록은_페이지_안의_이벤트_필드를_반환한다() throws Exception {
        when(eventService.list(eq("ongoing"), any(Pageable.class))).thenReturn(new PageResponse<>(
                List.of(sample()), 0, 20, 1, 1));

        mvc.perform(get("/events").param("status", "ongoing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("11월 찍먹 챌린지"))
                .andExpect(jsonPath("$.data.content[0].status").value("ongoing"))
                .andExpect(jsonPath("$.data.content[0].participantCount").value(0))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void 없거나_삭제된_이벤트는_404이다() throws Exception {
        when(eventService.get(9L)).thenThrow(new BusinessException(ErrorCode.NOT_FOUND, "이벤트를 찾을 수 없어요."));

        mvc.perform(get("/events/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    private static EventResponse sample() {
        return new EventResponse(
                1L,
                "11월 찍먹 챌린지",
                "하루 만에 미니 게임을 올려 주세요.",
                "하루 만에 미니 게임을 올려 주세요.",
                "ongoing",
                "2026-11-01T00:00:00+09:00",
                "2026-11-30T23:59:00+09:00",
                "1등 300칩",
                "🎉",
                0,
                "이벤트 참여하기",
                "/demos",
                null);
    }
}
