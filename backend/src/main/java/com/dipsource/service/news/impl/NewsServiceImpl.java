package com.dipsource.service.news.impl;

import com.dipsource.dto.news.NewsItemResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.news.NewsService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** {@link NewsService} 구현체. */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsFeedSource feedSource;
    private final LastNewsSnapshot snapshot;

    @Override
    public List<NewsItemResponse> search(String q, String category) {
        List<NewsItemDraft> all = loadAll();
        String query = normalize(q);
        String selected = normalize(category);
        List<NewsItemResponse> result = new ArrayList<>();
        long id = 1;
        for (NewsItemDraft item : all) {
            if (!selected.isEmpty() && !item.category().equals(selected)) {
                continue;
            }
            if (!query.isEmpty() && !haystack(item).contains(query)) {
                continue;
            }
            result.add(new NewsItemResponse(
                    id++,
                    item.title(),
                    item.summary(),
                    item.source(),
                    item.category(),
                    item.publishedAt(),
                    item.url()
            ));
        }
        return List.copyOf(result);
    }

    private List<NewsItemDraft> loadAll() {
        try {
            return feedSource.load();
        } catch (RuntimeException ex) {
            List<NewsItemDraft> last = snapshot.latest();
            if (last != null) {
                log.warn("뉴스 RSS 조회에 실패해 마지막 성공 목록을 반환합니다", ex);
                return last;
            }
            throw new BusinessException(ErrorCode.NEWS_FETCH_FAILED);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String haystack(NewsItemDraft item) {
        return (item.title() + " " + item.summary()).toLowerCase(Locale.ROOT);
    }
}
