package com.dipsource.dto.feed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 피드 수정 요청 DTO
 * PUT /feeds/{feedId} 에서 사용
 * 수정 가능한 필드는 본문(content)만 허용함
 */
public record FeedUpdateRequest(

                /** 수정할 피드 본문. 필수, 최대 500자 */
                @NotBlank @Size(max = 500) String content) {
}
