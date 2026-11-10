package com.dipsource.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** 이벤트 등록·수정 요청. status는 폼이 보내도 저장하지 않는다. */
public record SaveEventRequest(
        @NotBlank(message = "제목을 입력해 주세요.") @Size(max = 100, message = "제목은 100자 이하여야 합니다.") String title,
        @Size(max = 8, message = "이모지는 8자 이하여야 합니다.") String emoji,
        @NotBlank(message = "한 줄 소개를 입력해 주세요.") @Size(max = 200, message = "한 줄 소개는 200자 이하여야 합니다.") String summary,
        @NotBlank(message = "보상을 입력해 주세요.") @Size(max = 100, message = "보상은 100자 이하여야 합니다.") String reward,
        @NotNull(message = "시작일을 입력해 주세요.") LocalDate startAt,
        @NotNull(message = "종료일을 입력해 주세요.") LocalDate endAt,
        String status
) {
}
