package com.dipsource.service.search.personal;

import com.dipsource.dto.search.SearchExclusions;
import com.dipsource.dto.search.SearchType;
import com.dipsource.entity.search.SearchVisit;
import com.dipsource.global.config.SearchProperties;
import com.dipsource.global.config.SearchProperties.Personalization;
import com.dipsource.repository.block.BlockSearchRepository;
import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.repository.search.SearchHiddenResultRepository;
import com.dipsource.repository.search.SearchVisitRepository;
import com.dipsource.service.search.query.ParsedQuery;
import com.dipsource.service.search.query.TextNormalizer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자별 검색 데이터. 사용자별 인덱스를 만들지 않고 PostgreSQL에서 후보 단위로 한 번에 읽어 2차 재정렬에 쓴다.
 * 비로그인이면 모두 빈 값이라 개인화가 적용되지 않는다.
 */
@Service
@RequiredArgsConstructor
public class SearchPersonalizationService {

    private final SearchProperties properties;
    private final BlockSearchRepository blockRepository;
    private final SearchHiddenResultRepository hiddenRepository;
    private final SearchVisitRepository visitRepository;
    private final FollowRepository followRepository;

    /** PostgreSQL 검색에 넘길 제외 대상. 비로그인이면 쿼리를 하지 않는다 */
    @Transactional(readOnly = true)
    public SearchExclusions exclusions(Long viewerId) {
        if (viewerId == null) {
            return SearchExclusions.NONE;
        }
        Map<SearchType, List<Long>> hidden = new EnumMap<>(SearchType.class);
        for (SearchType type : List.of(SearchType.POST, SearchType.DEMO, SearchType.FEED, SearchType.USER)) {
            hidden.put(type, List.copyOf(hiddenIds(viewerId, type)));
        }
        return new SearchExclusions(hidden, blockedUsers(viewerId));
    }

    @Transactional(readOnly = true)
    public Set<Long> blockedUsers(Long viewerId) {
        return viewerId == null ? Set.of() : new HashSet<>(blockRepository.findBlockRelatedUserIds(viewerId));
    }

    @Transactional(readOnly = true)
    public Set<Long> hiddenIds(Long viewerId, SearchType type) {
        if (viewerId == null) {
            return Set.of();
        }
        return new HashSet<>(hiddenRepository.findTargetIds(viewerId, type.name(),
                properties.getPersonalization().getMaxHiddenFilter()));
    }

    /** 후보별 개인화 가산. 상한과 조건은 설정을 따른다. 요청당 쿼리 수는 후보 수와 상관없이 일정하다 */
    @Transactional(readOnly = true)
    public Map<Long, Double> personalBoosts(Long viewerId, SearchType type, ParsedQuery query, Collection<Long> candidateIds) {
        Personalization policy = properties.getPersonalization();
        if (viewerId == null || !policy.isEnabled() || candidateIds.isEmpty()) {
            return Map.of();
        }
        return type == SearchType.USER
                ? mutualFollowBoosts(viewerId, candidateIds, policy)
                : visitBoosts(viewerId, type, query, candidateIds, policy);
    }

    private Map<Long, Double> mutualFollowBoosts(Long viewerId, Collection<Long> candidateIds, Personalization policy) {
        return followRepository.countMutualFollows(viewerId, candidateIds).stream()
                .collect(Collectors.toMap(FollowRepository.MutualFollowCount::getUserId, row -> {
                    double count = row.getMutualCount();
                    return policy.getMaxMutualBoost() * count / (count + policy.getMutualSaturation());
                }));
    }

    private Map<Long, Double> visitBoosts(Long viewerId, SearchType type, ParsedQuery query, Collection<Long> candidateIds,
                                          Personalization policy) {
        List<SearchVisit> visits = visitRepository.findRecent(viewerId, type.name(), candidateIds, policy.getMinVisits(),
                LocalDateTime.now().minus(policy.getVisitRecency()));
        Set<String> current = new HashSet<>(query.keywords());
        Map<Long, Double> boosts = new HashMap<>();
        for (SearchVisit visit : visits) {
            if (similarity(current, visit.getQueryNorm()) < policy.getMinQuerySimilarity()) {
                continue;
            }
            double ageDays = Duration.between(visit.getLastVisitedAt(), LocalDateTime.now()).toHours() / 24.0;
            double boost = policy.getMaxVisitBoost() * Math.pow(0.5, ageDays / policy.getVisitHalfLifeDays());
            boosts.merge(visit.getTargetId(), boost, Math::max);
        }
        return boosts;
    }

    /** 단어 집합의 Jaccard 유사도 */
    private static double similarity(Set<String> current, String visitedQuery) {
        Set<String> visited = new HashSet<>(Arrays.asList(TextNormalizer.normalize(visitedQuery).split(" ")));
        Set<String> union = new HashSet<>(current);
        union.addAll(visited);
        Set<String> intersection = new HashSet<>(current);
        intersection.retainAll(visited);
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }
}
