package com.dipsource.dto.demo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 데모 피드백 수정 요청 */
public record DemoCommentUpdateRequest(@NotBlank @Size(max = 1000) String content) {
}
