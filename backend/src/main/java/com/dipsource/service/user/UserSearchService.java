package com.dipsource.service.user;

import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchSignal;
import com.dipsource.dto.user.UserSearchItemResponse;
import com.dipsource.global.common.PageResponse;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;

/** 사람 검색. 통합 검색(search)에서 쓴다. */
public interface UserSearchService {

    /**
     * 활동 중인 회원을 닉네임·자기소개·기술 스택으로 검색한다. 정지·탈퇴 회원은 빠진다.
     *
     * @param condition 정리된 검색어, and/or, 정렬
     */
    PageResponse<UserSearchItemResponse> search(SearchCondition condition, Pageable pageable);

    /** 재정렬 신호. 공개 상태가 아닌 회원은(는) 빠진다 */
    List<SearchSignal> findSignals(Collection<Long> ids);

    /** 검색 결과 항목을 주어진 ID 순서대로 만든다 */
    List<UserSearchItemResponse> findItems(List<Long> ids);
}
