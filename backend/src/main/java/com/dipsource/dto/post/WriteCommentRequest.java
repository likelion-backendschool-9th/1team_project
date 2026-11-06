package com.dipsource.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 블로그 댓글 작성·수정 요청.
 *
 * @param content  1~300자 (화면 기준. DB는 1000자까지)
 * @param parentId 답글이면 부모 댓글 ID. 수정할 때는 쓰지 않는다
 */
public record WriteCommentRequest(
        @NotBlank @Size(max = 300) String content,
        Long parentId
) {
}
