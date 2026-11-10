package com.dipsource.dto.dm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DM 보내기 요청. STOMP /app/dm.send 로 받는다.
 *
 * @param receiverId 받는 회원 ID
 * @param content    내용(1~1000자)
 */
public record DmSendRequest(
        @NotNull Long receiverId,
        @NotBlank @Size(max = 1000) String content
) {
}
