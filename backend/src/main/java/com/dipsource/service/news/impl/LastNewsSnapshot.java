package com.dipsource.service.news.impl;

import java.util.List;
import org.springframework.stereotype.Component;

/** 마지막으로 성공한 전체 목록. 만료 없이 실패 때만 쓴다. */
@Component
public class LastNewsSnapshot {

    private volatile List<NewsItemDraft> items;

    public void save(List<NewsItemDraft> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("빈 목록은 저장하지 않습니다");
        }
        this.items = List.copyOf(items);
    }

    /** 한 번도 성공한 적이 없으면 null. */
    public List<NewsItemDraft> latest() {
        return items;
    }
}
