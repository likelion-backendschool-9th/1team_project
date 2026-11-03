package com.dipsource.service.news.impl;

import java.util.List;

/** 필터 전 전체 뉴스 목록. */
public interface NewsFeedSource {

    List<NewsItemDraft> load();
}
