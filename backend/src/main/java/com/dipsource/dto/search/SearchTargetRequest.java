package com.dipsource.dto.search;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 검색 결과 숨김·방문 기록 요청.
 *
 * @param targetType post, demo, feed, user
 * @param targetId   대상 ID
 * @param query      결과를 찾은 검색어. 방문 기록에서만 쓴다
 */
public record SearchTargetRequest(@NotNull String targetType, @NotNull Long targetId, @Size(max = 100) String query) {
}
