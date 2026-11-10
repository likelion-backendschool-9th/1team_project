package com.dipsource.service.news.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class NewsServiceTest {

    @Test
    void filtersAfterTheFullListAndRenumbersFromOne() {
        NewsServiceImpl service = new NewsServiceImpl(
                () -> List.of(
                        draft("React 19", "서버 액션", "frontend"),
                        draft("Spring Boot", "가상 스레드", "backend"),
                        draft("React Native", "모바일", "frontend")
                ),
                new LastNewsSnapshot()
        );

        var result = service.search("react", "frontend");

        assertThat(result).extracting(item -> item.title()).containsExactly("React 19", "React Native");
        assertThat(result).extracting(item -> item.id()).containsExactly(1L, 2L);
    }

    @Test
    void blankQueryAndCategoryKeepTheWholeList() {
        NewsServiceImpl service = new NewsServiceImpl(
                () -> List.of(draft("아무 뉴스", "본문", "etc")),
                new LastNewsSnapshot()
        );

        assertThat(service.search("  ", null)).hasSize(1);
        assertThat(service.search(null, "  ")).hasSize(1);
    }

    @Test
    void usesLastSuccessfulListWhenLoadFails() {
        LastNewsSnapshot snapshot = new LastNewsSnapshot();
        snapshot.save(List.of(
                draft("보관된 React", "캐시", "frontend"),
                draft("보관된 Spring", "캐시", "backend")
        ));
        NewsServiceImpl service = new NewsServiceImpl(
                () -> {
                    throw new IllegalStateException("rss down");
                },
                snapshot
        );

        var result = service.search(null, "backend");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("보관된 Spring");
        assertThat(result.get(0).id()).isEqualTo(1L);
    }

    @Test
    void failsWhenThereIsNoSuccessfulList() {
        NewsServiceImpl service = new NewsServiceImpl(
                () -> {
                    throw new IllegalStateException("rss down");
                },
                new LastNewsSnapshot()
        );

        assertThatThrownBy(() -> service.search(null, null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NEWS_FETCH_FAILED);
    }

    private static NewsItemDraft draft(String title, String summary, String category) {
        return new NewsItemDraft(
                title,
                summary,
                "GeekNews",
                category,
                OffsetDateTime.parse("2026-11-03T10:00:00+09:00"),
                "https://news.hada.io/topic?id=1"
        );
    }
}
