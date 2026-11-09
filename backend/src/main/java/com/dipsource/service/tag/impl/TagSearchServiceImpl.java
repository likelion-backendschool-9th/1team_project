package com.dipsource.service.tag.impl;

import com.dipsource.dto.search.AutocompletePrefix;
import com.dipsource.dto.search.SearchTermCandidate;
import com.dipsource.dto.search.SearchTermFrequency;
import com.dipsource.repository.tag.TagSearchRepository;
import com.dipsource.service.tag.TagSearchService;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link TagSearchService} 구현체. */
@Service
@Timed(value = "search.query", description = "검색 쿼리 메서드별 실행 시간 (통합 검색 5단계 측정)")
@RequiredArgsConstructor
public class TagSearchServiceImpl implements TagSearchService {

    private final TagSearchRepository tagSearchRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SearchTermCandidate> findClosestTerms(String word, int maxDistance) {
        return tagSearchRepository.findClosestTerms(word, maxDistance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchTermFrequency> findTermsByPrefix(AutocompletePrefix prefix, int limit) {
        return tagSearchRepository.findTermsByPrefix(prefix, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findNamesByPrefix(AutocompletePrefix prefix, int limit) {
        return tagSearchRepository.findNamesByPrefix(prefix, limit);
    }
}
