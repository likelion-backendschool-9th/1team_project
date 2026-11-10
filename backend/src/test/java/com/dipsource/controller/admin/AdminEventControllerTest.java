package com.dipsource.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.dto.event.EventResponse;
import com.dipsource.dto.event.SaveEventRequest;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.global.exception.GlobalExceptionHandler;
import com.dipsource.service.event.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class AdminEventControllerTest {

    @Mock
    private EventService eventService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mvc = MockMvcBuilders.standaloneSetup(new AdminEventController(eventService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }

    @Test
    void 등록은_저장된_이벤트를_반환한다() throws Exception {
        when(eventService.create(any(SaveEventRequest.class))).thenReturn(sample());

        mvc.perform(post("/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"11월 찍먹 챌린지","summary":"하루 만에 미니 게임을 올려 주세요.","reward":"1등 300칩","startAt":"2026-11-01","endAt":"2026-11-30","status":"upcoming"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("ongoing"))
                .andExpect(jsonPath("$.data.emoji").value("🎉"))
                .andExpect(jsonPath("$.data.participantCount").value(0));
    }

    @Test
    void 제목이_없으면_400이다() throws Exception {
        mvc.perform(post("/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","summary":"소개","reward":"보상","startAt":"2026-11-01","endAt":"2026-11-30"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
        verify(eventService, never()).create(any());
    }

    @Test
    void 없는_이벤트_수정은_404이다() throws Exception {
        when(eventService.update(org.mockito.ArgumentMatchers.eq(4L), any(SaveEventRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND, "이벤트를 찾을 수 없어요."));

        mvc.perform(put("/admin/events/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"제목","summary":"소개","reward":"보상","startAt":"2026-11-01","endAt":"2026-11-30"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void 삭제는_빈_데이터를_반환하고_없으면_404이다() throws Exception {
        mvc.perform(delete("/admin/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        doThrow(new BusinessException(ErrorCode.NOT_FOUND, "이벤트를 찾을 수 없어요.")).when(eventService).delete(9L);
        mvc.perform(delete("/admin/events/9"))
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
                "데모 둘러보기",
                "/demos");
    }
}
