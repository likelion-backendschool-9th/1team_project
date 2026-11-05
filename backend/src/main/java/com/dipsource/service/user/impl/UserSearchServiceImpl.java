package com.dipsource.service.user.impl;

import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.user.UserSearchItemResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.repository.user.UserSearchRepository;
import com.dipsource.service.user.UserSearchService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link UserSearchService} 구현체. */
@Service
@Timed(value = "search.query", description = "검색 쿼리 메서드별 실행 시간 (통합 검색 5단계 측정)")
@RequiredArgsConstructor
public class UserSearchServiceImpl implements UserSearchService {

    private final UserSearchRepository userSearchRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserSearchItemResponse> search(SearchCondition condition, Pageable pageable) {
        return PageResponse.of(userSearchRepository.search(condition, pageable), UserSearchItemResponse::from);
    }
}
