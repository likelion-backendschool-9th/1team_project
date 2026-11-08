package com.dipsource.service.demo.impl;

import com.dipsource.dto.demo.DemoSearchItemResponse;
import com.dipsource.dto.search.AutocompletePrefix;
import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchSignal;
import com.dipsource.dto.search.SearchTermCandidate;
import com.dipsource.dto.search.SearchTermFrequency;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.global.common.PageResponse;
import com.dipsource.repository.demo.DemoSearchRepository;
import com.dipsource.service.demo.DemoSearchService;
import com.dipsource.service.user.UserService;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link DemoSearchService} 구현체. */
@Service
@Timed(value = "search.query", description = "검색 쿼리 메서드별 실행 시간 (통합 검색 5단계 측정)")
@RequiredArgsConstructor
public class DemoSearchServiceImpl implements DemoSearchService {

    private final DemoSearchRepository demoSearchRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DemoSearchItemResponse> search(SearchCondition condition, Pageable pageable) {
        Page<Demo> demos = demoSearchRepository.search(condition, pageable);
        Map<Long, UserSummaryResponse> authors = userService.getSummaries(
                demos.getContent().stream().map(Demo::getUserId).distinct().toList());
        return PageResponse.of(demos, demo -> toSearchItem(demo, authors.get(demo.getUserId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchTermCandidate> findClosestTerms(String word, int maxDistance) {
        return demoSearchRepository.findClosestTerms(word, maxDistance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchTermFrequency> findTermsByPrefix(AutocompletePrefix prefix, int limit) {
        return demoSearchRepository.findTermsByPrefix(prefix, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchSignal> findSignals(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : demoSearchRepository.findSignals(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemoSearchItemResponse> findItems(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Demo> demos = demoSearchRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(Demo::getId, Function.identity()));
        Map<Long, UserSummaryResponse> authors = userService.getSummaries(
                demos.values().stream().map(Demo::getUserId).distinct().toList());
        return ids.stream().map(demos::get).filter(Objects::nonNull)
                .map(demo -> toSearchItem(demo, authors.get(demo.getUserId())))
                .toList();
    }

    private static DemoSearchItemResponse toSearchItem(Demo demo, UserSummaryResponse author) {
        return new DemoSearchItemResponse(
                demo.getId(),
                demo.getTitle(),
                demo.getDescription(),
                demo.getDemoType().name(),
                demo.getThumbnail(),
                demo.getUserId(),
                author == null ? null : author.nickname(),
                demo.getLikeCount(),
                demo.getPlayCount(),
                demo.getCreatedAt()
        );
    }
}
