package com.dipsource.service.user.impl;

import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchSignal;
import com.dipsource.dto.user.UserSearchItemResponse;
import com.dipsource.entity.user.User;
import com.dipsource.global.common.PageResponse;
import com.dipsource.repository.user.UserSearchRepository;
import com.dipsource.service.user.UserSearchService;
import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    @Override
    @Transactional(readOnly = true)
    public List<SearchSignal> findSignals(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : userSearchRepository.findSignals(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchItemResponse> findItems(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, User> users = userSearchRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return ids.stream().map(users::get).filter(Objects::nonNull).map(UserSearchItemResponse::from).toList();
    }
}
