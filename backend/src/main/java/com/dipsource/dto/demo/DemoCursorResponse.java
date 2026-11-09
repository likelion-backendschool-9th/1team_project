package com.dipsource.dto.demo;

import java.util.List;

/** 데모 목록 커서 페이지네이션 래퍼 */
public record DemoCursorResponse(
        List<DemoResponse> content,
        String nextCursor,
        boolean hasNext,
        int size
) {
}
