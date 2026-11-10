package com.dipsource.controller.settlement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.GlobalExceptionHandler;
import com.dipsource.service.settlement.SettlementService;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class SettlementControllerTest {

    @Mock
    private SettlementService settlementService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new SettlementController(settlementService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 비로그인은_401이다() throws Exception {
        mvc.perform(get("/settlements/me"))
                .andExpect(status().isUnauthorized());

        verify(settlementService, never()).mySettlements(any(), anyInt(), anyInt());
    }

    @Test
    void 로그인은_200이다() throws Exception {
        login();
        when(settlementService.mySettlements(1L, 0, 20))
                .thenReturn(PageResponse.of(new PageImpl<>(List.of()), List.of()));

        mvc.perform(get("/settlements/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        verify(settlementService).mySettlements(1L, 0, 20);
    }

    @Test
    void 수익_내역은_월과_날짜를_같이_주거나_둘_다_없으면_400이다() throws Exception {
        login();

        mvc.perform(get("/settlements/me/incomes")).andExpect(status().isBadRequest());
        mvc.perform(get("/settlements/me/incomes").param("month", "2026-10").param("date", "2026-10-01"))
                .andExpect(status().isBadRequest());

        verify(settlementService, never()).myIncomes(any(), any(), any());
    }

    @Test
    void 수익_내역은_월만_있으면_200이다() throws Exception {
        login();
        when(settlementService.myIncomes(eq(1L), eq(YearMonth.of(2026, 10)), isNull())).thenReturn(List.of());

        mvc.perform(get("/settlements/me/incomes").param("month", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    private static void login() {
        UserPrincipal principal = new UserPrincipal(1L, "USER");
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }
}
