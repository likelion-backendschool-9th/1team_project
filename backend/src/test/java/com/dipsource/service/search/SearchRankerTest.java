package com.dipsource.service.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.dipsource.dto.search.SearchSignal;
import com.dipsource.dto.search.SearchType;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.service.search.ranking.SearchRanker;
import com.dipsource.service.search.ranking.SearchRanker.Candidate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchRankerTest {

    private final SearchRanker ranker = new SearchRanker(new SearchProperties());

    private static SearchSignal signal(int likes, int views, long followers, double ageDays) {
        return new SearchSignal() {
            public Long getId() { return 0L; }
            public Long getAuthorId() { return 0L; }
            public Integer getLikeCount() { return likes; }
            public Integer getViewCount() { return views; }
            public Double getAgeDays() { return ageDays; }
            public Long getFollowerCount() { return followers; }
        };
    }

    private static Candidate candidate(long id, double score, int likes) {
        return new Candidate(id, score, false, signal(likes, 0, 0, 10), 0);
    }

    @Test
    @DisplayName("관련도가 비슷하면 인기 많은 문서가 앞선다")
    void popularityWithinSimilarRelevance() {
        assertThat(ranker.rank(SearchType.POST, List.of(candidate(1, 10.0, 0), candidate(2, 9.5, 500))))
                .containsExactly(2L, 1L);
    }

    @Test
    @DisplayName("관련도 차이가 상한보다 크면 좋아요가 아무리 많아도 뒤집히지 않는다")
    void popularityCannotOvertakeLargeGap() {
        assertThat(ranker.rank(SearchType.POST, List.of(candidate(1, 10.0, 0), candidate(2, 5.0, 1_000_000))))
                .containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("점수 범위가 달라도 같은 상대 차이면 같은 결과다")
    void scaleInvariant() {
        assertThat(ranker.rank(SearchType.POST, List.of(candidate(1, 100.0, 0), candidate(2, 95.0, 500))))
                .containsExactly(2L, 1L);
        assertThat(ranker.rank(SearchType.POST, List.of(candidate(1, 1.0, 0), candidate(2, 0.95, 500))))
                .containsExactly(2L, 1L);
    }

    @Test
    @DisplayName("정확 일치는 점수와 상관없이 맨 앞이다")
    void exactMatchFirst() {
        Candidate exact = new Candidate(1, 1.0, true, signal(0, 0, 0, 100), 0);
        assertThat(ranker.rank(SearchType.USER, List.of(candidate(2, 50.0, 0), exact))).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("관련도가 하한보다 낮으면 인기도 가산을 받지 못한다")
    void noBoostBelowFloor() {
        Candidate top = candidate(1, 100.0, 0);
        Candidate low = candidate(2, 20.0, 1_000_000);
        Candidate lower = candidate(3, 20.0, 0);
        assertThat(ranker.rank(SearchType.POST, List.of(top, lower, low))).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("모두 같으면 ID로 순서가 고정된다")
    void tieBreakById() {
        assertThat(ranker.rank(SearchType.POST, List.of(candidate(1, 5.0, 0), candidate(3, 5.0, 0), candidate(2, 5.0, 0))))
                .containsExactly(3L, 2L, 1L);
    }

    @Test
    @DisplayName("개인화 가산은 재정렬에 반영된다")
    void personalBoostApplied() {
        Candidate personal = candidate(2, 9.5, 0).withPersonalBoost(0.1);
        assertThat(ranker.rank(SearchType.POST, List.of(candidate(1, 10.0, 0), personal))).containsExactly(2L, 1L);
    }
}
