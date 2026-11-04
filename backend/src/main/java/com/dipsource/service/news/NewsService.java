package com.dipsource.service.news;

import com.dipsource.dto.news.NewsItemResponse;
import java.util.List;

/** 개발 뉴스 검색. */
public interface NewsService {

    /** 전체 목록을 꺼낸 뒤 검색어와 분야로 거른다. */
    List<NewsItemResponse> search(String q, String category);
}
