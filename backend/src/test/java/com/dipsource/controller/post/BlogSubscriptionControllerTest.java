package com.dipsource.controller.post;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.exception.GlobalExceptionHandler;
import com.dipsource.service.post.BlogSubscriptionService;
import com.dipsource.service.user.PlusSubscriptionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class BlogSubscriptionControllerTest {

    @Mock
    private BlogSubscriptionService blogSubscriptionService;
    @Mock
    private PlusSubscriptionService plusSubscriptionService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        UserPrincipal principal = new UserPrincipal(1L, "USER");
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
        mvc = MockMvcBuilders.standaloneSetup(
                        new BlogSubscriptionController(blogSubscriptionService, plusSubscriptionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 플러스_시작은_204이고_블로그_구독은_호출하지_않는다() throws Exception {
        mvc.perform(post("/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plan\":\"plus\"}"))
                .andExpect(status().isNoContent());

        verify(plusSubscriptionService).start(1L);
        verify(blogSubscriptionService, never()).subscribe(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 플러스_해지는_204이고_블로그_해지는_호출하지_않는다() throws Exception {
        mvc.perform(delete("/subscriptions/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plan\":\"plus\"}"))
                .andExpect(status().isNoContent());

        verify(plusSubscriptionService).cancel(1L);
        verify(blogSubscriptionService, never()).cancel(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 블로그_구독은_기존_서비스만_호출한다() throws Exception {
        mvc.perform(post("/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":2}"))
                .andExpect(status().isNoContent());

        verify(blogSubscriptionService).subscribe(1L, 2L);
        verify(plusSubscriptionService, never()).start(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 블로그_해지는_기존_서비스만_호출한다() throws Exception {
        mvc.perform(delete("/subscriptions/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":9}"))
                .andExpect(status().isNoContent());

        verify(blogSubscriptionService).cancel(1L, 9L);
        verify(plusSubscriptionService, never()).cancel(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 알_수_없는_플랜은_400이다() throws Exception {
        mvc.perform(post("/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plan\":\"gold\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        verify(plusSubscriptionService, never()).start(org.mockito.ArgumentMatchers.any());
        verify(blogSubscriptionService, never()).subscribe(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
