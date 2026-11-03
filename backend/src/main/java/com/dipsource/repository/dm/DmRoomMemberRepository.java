package com.dipsource.repository.dm;

import com.dipsource.entity.dm.DmRoomMember;
import com.dipsource.entity.dm.DmRoomMemberId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** dm_room_members 테이블 접근. */
public interface DmRoomMemberRepository extends JpaRepository<DmRoomMember, DmRoomMemberId> {

    /** 대화방의 참여자 목록 */
    List<DmRoomMember> findByIdRoomId(Long roomId);

    /** 회원이 나가지 않고 참여 중인 대화방 목록 */
    List<DmRoomMember> findByIdUserIdAndLeftAtIsNull(Long userId);
}
