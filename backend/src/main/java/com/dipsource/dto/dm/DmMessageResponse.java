package com.dipsource.dto.dm;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.dm.DmMessage;
import java.time.LocalDateTime;

/**
 * 실시간으로 전달하는 DM 메시지. 보낸 사람과 받는 사람 모두 /user/queue/dm 으로 받는다.
 *
 * @param id         메시지 ID
 * @param roomId     대화방 ID
 * @param senderId   보낸 회원 ID
 * @param sender     보낸 회원 요약. 받는 사람 화면의 새 DM 알림에 닉네임을 띄울 때 쓴다
 * @param receiverId 받는 회원 ID
 * @param content    내용
 * @param createdAt  보낸 시각
 */
public record DmMessageResponse(
        Long id,
        Long roomId,
        Long senderId,
        UserSummaryResponse sender,
        Long receiverId,
        String content,
        LocalDateTime createdAt
) {

    public static DmMessageResponse of(DmMessage message, UserSummaryResponse sender, Long receiverId) {
        return new DmMessageResponse(
                message.getId(),
                message.getRoom().getId(),
                message.getSenderId(),
                sender,
                receiverId,
                message.getContent(),
                message.getCreatedAt());
    }
}
