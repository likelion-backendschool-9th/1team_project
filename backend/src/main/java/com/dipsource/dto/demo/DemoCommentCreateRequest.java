package com.dipsource.dto.demo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 데모 피드백 작성 요청. type은 question·bug·praise */
public record DemoCommentCreateRequest(
        @NotBlank @Size(max = 1000) String content,
        Long parentId,
        String type) {
}
