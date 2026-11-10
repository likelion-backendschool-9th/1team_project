package com.dipsource.dto.minihome;

import java.util.List;

/** 미니홈피 조회·수정 응답. 회원 정보는 포함하지 않는다. */
public record MiniHomeResponse(
        String headline,
        String quote,
        String skin,
        String domain,
        String bgm,
        List<Long> showcaseDemoIds,
        Long featuredDemoId,
        List<String> stackTags,
        Long featuredPostId
) {
}
