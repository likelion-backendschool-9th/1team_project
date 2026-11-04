package com.dipsource.dto.post;

import java.time.LocalDateTime;

/**
 * 검색 결과의 블로그 글 한 건.
 *
 * @param id        글 ID
 * @param title     제목
 * @param excerpt   본문 앞부분. 유료 글은 본문을 보여 주지 않으므로 null(응답에서 빠짐)
 * @param authorId  작성자 ID
 * @param author    작성자 닉네임
 * @param price     유료 글 가격(칩). 무료 글은 0
 * @param createdAt 작성 시각
 */
public record PostSearchItemResponse(
        Long id,
        String title,
        String excerpt,
        Long authorId,
        String author,
        int price,
        LocalDateTime createdAt
) {
}
