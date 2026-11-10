package com.dipsource.dto.dm;

/**
 * 상대가 내 메시지를 어디까지 읽었는지. 보낸 사람이 /user/queue/dm-read 로 받아 "1" 표시를 지운다.
 *
 * @param readerId          읽은 회원 ID
 * @param lastReadMessageId 이 ID까지의 메시지를 읽었다
 */
public record DmReadResponse(Long readerId, Long lastReadMessageId) {
}
