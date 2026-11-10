package com.dipsource.dto.dm;

import jakarta.validation.constraints.NotNull;

/**
 * 대화를 보고 있는 동안 받은 메시지를 읽음 처리하는 요청. STOMP /app/dm.read 로 받는다.
 *
 * @param partnerId 대화 상대 회원 ID
 */
public record DmReadRequest(@NotNull Long partnerId) {
}
