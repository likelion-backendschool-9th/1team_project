package com.dipsource.dto.feed;

import java.time.LocalDateTime;

/**
 * 검색 결과의 피드 한 건.
 *
 * @param id           피드 ID
 * @param content      본문(최대 500자라 그대로 준다)
 * @param authorId     작성자 ID
 * @param author       작성자 닉네임
 * @param likeCount    좋아요 수
 * @param commentCount 댓글 수
 * @param createdAt    작성 시각
 */
public record FeedSearchItemResponse(
        Long id,
        String content,
        Long authorId,
        String author,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt
) {
}
