package com.dipsource.service.news.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class CachingNewsFeedTest {

    @Test
    void emptyFeedDoesNotReplaceTheLastSuccess() {
        LastNewsSnapshot snapshot = new LastNewsSnapshot();
        NewsItemDraft kept = draft("보관된 글");
        snapshot.save(List.of(kept));
        CachingNewsFeed feed = new CachingNewsFeed(clientReturning(List.of()), snapshot);

        assertThatThrownBy(feed::load).isInstanceOf(IllegalStateException.class);
        assertThat(snapshot.latest()).containsExactly(kept);
    }

    @Test
    void successfulFeedReplacesTheSnapshot() {
        LastNewsSnapshot snapshot = new LastNewsSnapshot();
        NewsItemDraft next = draft("새 글");
        CachingNewsFeed feed = new CachingNewsFeed(clientReturning(List.of(next)), snapshot);

        assertThat(feed.load()).containsExactly(next);
        assertThat(snapshot.latest()).containsExactly(next);
    }

    private static GeekNewsClient clientReturning(List<NewsItemDraft> items) {
        return new GeekNewsClient(new NewsRssParser()) {
            @Override
            public List<NewsItemDraft> fetch() {
                return items;
            }
        };
    }

    private static NewsItemDraft draft(String title) {
        return new NewsItemDraft(
                title,
                "요약",
                "GeekNews",
                "etc",
                OffsetDateTime.parse("2026-11-03T10:00:00+09:00"),
                "https://news.hada.io/topic?id=1"
        );
    }
}
