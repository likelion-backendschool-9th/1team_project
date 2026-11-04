package com.dipsource.dto.dm;

import com.dipsource.dto.user.UserSummaryResponse;
import java.time.LocalDateTime;

/**
 * DM 대화방 목록 한 줄. GET /api/v1/dm/rooms
 *
 * @param user        대화 상대
 * @param online      상대가 접속 중인지(최근 5분 안 활동)
 * @param lastMessage 마지막 메시지 내용
 * @param lastAt      마지막 메시지 시각
 * @param unreadCount 내가 안 읽은 메시지 수
 */
public record DmRoomResponse(
        UserSummaryResponse user,
        boolean online,
        String lastMessage,
        LocalDateTime lastAt,
        long unreadCount
) {
}
