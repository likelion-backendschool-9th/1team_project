package com.dipsource.dto.feed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 댓글 수정 요청 */
public record CommentUpdateRequest(
        @NotBlank
        @Size(max = 500)
        @Pattern(regexp = "^(?!.*<[^>]+>).*$", message = "HTML 태그는 사용할 수 없습니다")
        String content
) {}
