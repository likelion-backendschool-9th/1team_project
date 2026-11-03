package com.dipsource.service.dm;

import com.dipsource.dto.dm.DmMessageItemResponse;
import com.dipsource.dto.dm.DmMessageResponse;
import com.dipsource.dto.dm.DmRoomResponse;
import com.dipsource.dto.dm.DmSendRequest;
import com.dipsource.global.common.PageResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

/** DM 보내기·대화방 목록·대화 내역. */
public interface DmService {

    /** 1:1 대화방이 없으면 만들고 메시지를 저장한다. */
    DmMessageResponse send(Long senderId, DmSendRequest request);

    /** 내가 참여 중인 대화방 목록. 최근 메시지 순 */
    List<DmRoomResponse> getRooms(Long userId);

    /** 상대와의 대화 내역. 오래된 순으로 담고, 읽음 처리한다. 대화방이 없으면 빈 목록 */
    PageResponse<DmMessageItemResponse> getMessages(Long userId, Long partnerId, Pageable pageable);
}
