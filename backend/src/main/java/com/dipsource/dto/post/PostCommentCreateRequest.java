package com.dipsource.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 블로그 댓글 작성 요청 */
public record PostCommentCreateRequest(
        @NotBlank @Size(max = 1000) String content,
        Long parentId) {
}
