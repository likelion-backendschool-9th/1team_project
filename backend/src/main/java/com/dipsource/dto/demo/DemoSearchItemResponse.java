package com.dipsource.dto.demo;

import java.time.LocalDateTime;

/**
 * 검색 결과의 데모 한 건.
 *
 * @param id          데모 ID
 * @param title       제목
 * @param description 설명
 * @param demoType    실행 방식(STATIC, API, EXTERNAL)
 * @param thumbnail   썸네일 URL
 * @param authorId    작성자 ID
 * @param author      작성자 닉네임
 * @param likeCount   좋아요 수
 * @param playCount   체험 수
 * @param createdAt   등록 시각
 */
public record DemoSearchItemResponse(
        Long id,
        String title,
        String description,
        String demoType,
        String thumbnail,
        Long authorId,
        String author,
        int likeCount,
        int playCount,
        LocalDateTime createdAt
) {
}
