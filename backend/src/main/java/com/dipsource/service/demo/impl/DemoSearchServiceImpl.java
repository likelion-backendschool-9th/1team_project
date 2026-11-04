package com.dipsource.service.demo.impl;

import com.dipsource.dto.demo.DemoSearchItemResponse;
import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.global.common.PageResponse;
import com.dipsource.repository.demo.DemoSearchRepository;
import com.dipsource.service.demo.DemoSearchService;
import com.dipsource.service.user.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link DemoSearchService} 구현체. */
@Service
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
        return PageResponse.of(demos, demo -> {
            UserSummaryResponse author = authors.get(demo.getUserId());
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
        });
    }
}
