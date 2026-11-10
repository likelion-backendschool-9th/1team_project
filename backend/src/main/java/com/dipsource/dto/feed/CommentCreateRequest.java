package com.dipsource.dto.feed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 댓글 작성 요청 */
public record CommentCreateRequest(
                @NotBlank @Size(max = 500) String content,
                Long parentId) {
}
