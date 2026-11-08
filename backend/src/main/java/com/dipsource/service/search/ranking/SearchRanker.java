package com.dipsource.service.search.ranking;

import com.dipsource.dto.search.SearchSignal;
import com.dipsource.dto.search.SearchType;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.global.config.SearchProperties.Popularity;
import com.dipsource.service.search.engine.SearchPort;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 2차 재정렬. BM25 점수를 그 검색어의 1위 점수로 나눠 상대 관련도로 바꾼 뒤 제한된 가산점을 곱한다.
 * 가산점에 상한이 있으므로, 상대 관련도 차이가 상한보다 큰 문서는 인기도로 순위가 뒤집히지 않는다.
 * 점수가 소수점까지 같을 때만 인기도를 보는 방식과 달리, 관련도가 비슷한 문서 사이에서 인기도가 작동한다.
 * 정확 일치(제목·닉네임 전체)는 숫자 부스트로 보장되지 않으므로 별도 등급으로 맨 앞에 둔다.
 */
@Component
@RequiredArgsConstructor
public class SearchRanker {

    /**
     * @param score         1차 검색 점수
     * @param exactMatch    제목·닉네임 전체 일치
     * @param personalBoost 개인화 가산점(0 이상). 상한은 계산하는 쪽이 지킨다
     */
    public record Candidate(long id, double score, boolean exactMatch, SearchSignal signal, double personalBoost) {

        public static Candidate from(SearchPort.Hit hit, SearchSignal signal) {
            return new Candidate(hit.id(), hit.score(), hit.exactMatch(), signal, 0);
        }

        public Candidate withPersonalBoost(double boost) {
            return new Candidate(id, score, exactMatch, signal, boost);
        }
    }

    private record Scored(Candidate candidate, double finalScore) {
    }

    private final SearchProperties properties;

    public List<Long> rank(SearchType type, List<Candidate> candidates) {
        if (properties.getRanking().isFlat() || candidates.isEmpty()) {
            return candidates.stream().map(Candidate::id).toList();
        }
        Popularity popularity = properties.getRanking().policy(type.name().toLowerCase(Locale.ROOT)).getPopularity();
        double top = candidates.stream().mapToDouble(Candidate::score).max().orElse(1);
        double divisor = top <= 0 ? 1 : top;
        double floor = properties.getRanking().getMinRelevanceForBoost();

        Comparator<Scored> order = Comparator
                .comparing((Scored s) -> s.candidate().exactMatch()).reversed()
                .thenComparing(Scored::finalScore, Comparator.reverseOrder())
                .thenComparing(s -> s.candidate().signal().getLikeCount(), Comparator.reverseOrder())
                .thenComparing(s -> s.candidate().signal().getViewCount(), Comparator.reverseOrder())
                .thenComparing(s -> s.candidate().signal().getFollowerCount(), Comparator.reverseOrder())
                .thenComparing(s -> s.candidate().signal().getAgeDays())
                .thenComparing(s -> s.candidate().id(), Comparator.reverseOrder());

        return candidates.stream()
                .map(candidate -> {
                    double relevance = candidate.score() / divisor;
                    double boost = relevance < floor ? 0 : boost(candidate, popularity);
                    return new Scored(candidate, relevance * (1 + boost));
                })
                .sorted(order)
                .map(scored -> scored.candidate().id())
                .toList();
    }

    private static double boost(Candidate candidate, Popularity policy) {
        SearchSignal signal = candidate.signal();
        double popularity = policy.getLikeWeight() * saturate(signal.getLikeCount(), policy.getLikeSaturation())
                + policy.getViewWeight() * saturate(signal.getViewCount(), policy.getViewSaturation())
                + policy.getFollowerWeight() * saturate(signal.getFollowerCount(), policy.getFollowerSaturation())
                + policy.getRecencyWeight() * Math.pow(0.5, signal.getAgeDays() / policy.getRecencyHalfLifeDays());
        return popularity * policy.getMaxBoost() + candidate.personalBoost();
    }

    /** 0~1로 포화. 큰 숫자가 무한정 점수를 올리지 못한다 */
    private static double saturate(double value, double halfPoint) {
        return value / (value + halfPoint);
    }
}
