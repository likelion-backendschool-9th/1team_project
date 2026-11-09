package com.dipsource.dto.demo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 데모 등록·수정 요청. 파일은 버전 업로드로 따로 올린다. */
public record DemoCreateRequest(
        @NotBlank(message = "데모 이름을 입력해 주세요") @Size(max = 100, message = "데모 이름은 100자 이하여야 합니다") String title,
        @Size(max = 2000, message = "설명은 2000자 이하여야 합니다") String description,
        @Size(max = 10, message = "태그는 10개까지 달 수 있습니다") List<@Size(max = 30, message = "태그는 30자 이하여야 합니다") String> tags
) {
}
