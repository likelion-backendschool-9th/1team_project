package com.dipsource.controller.search;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.global.exception.GlobalExceptionHandler;
import com.dipsource.service.search.AutocompleteService;
import com.dipsource.service.search.PopularSearchService;
import com.dipsource.service.search.SearchService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PopularSearchControllerTest {

    @Mock
    private SearchService searchService;
    @Mock
    private AutocompleteService autocompleteService;
    @Mock
    private PopularSearchService popularSearchService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new SearchController(searchService, autocompleteService, popularSearchService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 기본_조회는_열_개의_검색어를_반환한다() throws Exception {
        when(popularSearchService.popular(10)).thenReturn(List.of("spring", "react"));

        mvc.perform(get("/search/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("spring"))
                .andExpect(jsonPath("$.data[1]").value("react"));
    }

    @Test
    void 개수가_0이면_400이다() throws Exception {
        when(popularSearchService.popular(0))
                .thenThrow(new BusinessException(ErrorCode.BAD_REQUEST, "개수는 1 이상이어야 해요."));

        mvc.perform(get("/search/popular").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }
}
