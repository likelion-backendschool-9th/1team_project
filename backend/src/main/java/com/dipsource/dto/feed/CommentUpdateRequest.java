package com.dipsource.dto.feed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 댓글 수정 요청 */
public record CommentUpdateRequest(
        @NotBlank
        @Size(max = 500)
        String content
) {}
