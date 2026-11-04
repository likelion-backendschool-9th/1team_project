package com.dipsource.dto.search;

import com.dipsource.dto.demo.DemoSearchItemResponse;
import com.dipsource.dto.feed.FeedSearchItemResponse;
import com.dipsource.dto.post.PostSearchItemResponse;
import com.dipsource.dto.user.UserSearchItemResponse;

/**
 * 전체 검색(type=all) 응답. 대상별로 상위 몇 건과 총개수를 준다.
 * 대상끼리 하나의 순위로 섞지 않는다(관련도 점수가 생기는 단계에서 검토).
 *
 * @param correctedQuery 모든 대상이 0건이라 오타를 고쳐서 다시 검색했을 때의 검색어. 고치지 않았으면 null(응답에서 빠짐)
 */
public record SearchAllResponse(
        SearchSection<PostSearchItemResponse> posts,
        SearchSection<FeedSearchItemResponse> feeds,
        SearchSection<DemoSearchItemResponse> demos,
        SearchSection<UserSearchItemResponse> users,
        String correctedQuery
) {
}
