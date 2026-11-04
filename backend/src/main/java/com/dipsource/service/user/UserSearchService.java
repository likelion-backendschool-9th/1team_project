package com.dipsource.service.user;

import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.user.UserSearchItemResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 사람 검색. 통합 검색(search)에서 쓴다. */
public interface UserSearchService {

    /**
     * 활동 중인 회원을 닉네임·자기소개·기술 스택으로 검색한다. 정지·탈퇴 회원은 빠진다.
     *
     * @param condition 정리된 검색어, and/or, 정렬
     */
    PageResponse<UserSearchItemResponse> search(SearchCondition condition, Pageable pageable);
}
