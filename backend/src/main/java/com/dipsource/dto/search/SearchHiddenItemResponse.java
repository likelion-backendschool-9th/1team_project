package com.dipsource.dto.search;

import java.time.LocalDateTime;

/**
 * 숨긴 검색 결과 한 건.
 *
 * @param label 제목·닉네임 등 목록에 보여 줄 이름. 대상이 삭제됐으면 null
 */
public record SearchHiddenItemResponse(String targetType, Long targetId, String label, LocalDateTime hiddenAt) {
}
