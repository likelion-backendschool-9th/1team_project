package com.dipsource.service.search.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.entity.search.SearchKeywordLog;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.search.SearchKeywordLogRepository;
import com.dipsource.repository.tag.TagRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PopularSearchServiceImplTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 10, 15, 0);

    @Mock
    private SearchKeywordLogRepository searchKeywordLogRepository;
    @Mock
    private TagRepository tagRepository;
    private PopularSearchServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        service = new PopularSearchServiceImpl(searchKeywordLogRepository, tagRepository, clock);
    }

    @Test
    void 검색어는_공백을_줄이고_소문자로_저장한다() {
        service.record("  Spring   BOOT ", 3L);

        ArgumentCaptor<SearchKeywordLog> saved = ArgumentCaptor.forClass(SearchKeywordLog.class);
        verify(searchKeywordLogRepository).save(saved.capture());
        assertThat(saved.getValue().getKeyword()).isEqualTo("spring boot");
        assertThat(saved.getValue().getUserId()).isEqualTo(3L);
        assertThat(saved.getValue().getSearchedAt()).isEqualTo(NOW);
    }

    @Test
    void 한_글자와_오십_자를_넘는_검색어는_남기지_않는다() {
        service.record("  가  ", 3L);
        service.record("a".repeat(51), null);

        verify(searchKeywordLogRepository, never()).save(any());
    }

    @Test
    void 같은_사용자가_십분_안에_같은_검색어를_다시_치면_한_번만_센다() {
        when(searchKeywordLogRepository.existsByUserIdAndKeywordAndSearchedAtGreaterThanEqual(3L, "spring", NOW.minusMinutes(10)))
                .thenReturn(true);

        service.record("Spring", 3L);

        verify(searchKeywordLogRepository, never()).save(any());
    }

    @Test
    void 비로그인_검색은_십분_중복_확인_없이_남긴다() {
        service.record("Spring", null);

        verify(searchKeywordLogRepository, never())
                .existsByUserIdAndKeywordAndSearchedAtGreaterThanEqual(any(), any(), any());
        verify(searchKeywordLogRepository).save(any());
    }

    @Test
    void 많이_검색된_순이고_같으면_최근_검색이_앞이다() {
        LocalDateTime older = NOW.minusHours(2);
        LocalDateTime newer = NOW.minusHours(1);
        when(searchKeywordLogRepository.countSince(NOW.minusDays(7))).thenReturn(List.of(
                row("spring", 2, older),
                row("java", 5, older),
                row("react", 2, newer)));

        assertThat(service.popular(10)).containsExactly("java", "react", "spring");
        verify(tagRepository, never()).findPopularNames(anyInt());
    }

    @Test
    void 기록이_없으면_많이_쓰인_태그_이름을_돌려준다() {
        when(searchKeywordLogRepository.countSince(eq(NOW.minusDays(7)))).thenReturn(List.of());
        when(tagRepository.findPopularNames(10)).thenReturn(List.of("Spring", "React"));

        assertThat(service.popular(10)).containsExactly("Spring", "React");
    }

    @Test
    void 개수가_0이면_400이다() {
        assertThatThrownBy(() -> service.popular(0))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    private static Object[] row(String keyword, long count, LocalDateTime latest) {
        return new Object[] { keyword, count, latest };
    }
}
