package com.dipsource.dto.dm;

import java.time.LocalDateTime;

/**
 * 대화 내역의 메시지 한 건. GET /dm/{userId}
 *
 * @param id        메시지 ID
 * @param senderId  보낸 회원 ID
 * @param content   내용
 * @param createdAt 보낸 시각
 * @param read      상대가 읽었는지. 내가 받은 메시지는 지금 읽었으므로 true
 */
public record DmMessageItemResponse(
        Long id,
        Long senderId,
        String content,
        LocalDateTime createdAt,
        boolean read
) {
}
