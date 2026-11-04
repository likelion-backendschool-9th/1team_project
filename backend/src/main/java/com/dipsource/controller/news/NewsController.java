package com.dipsource.controller.news;

import com.dipsource.dto.news.NewsItemResponse;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.news.NewsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** GeekNews 개발 뉴스 목록. */
@RestController
@RequestMapping("/api/v1/search/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ApiResponse<List<NewsItemResponse>> getNews(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.of(newsService.search(q, category));
    }
}
