package com.dipsource.entity.dm;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (room_id, user_id) 복합 PK */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DmRoomMemberId implements Serializable {

    /** 대화방 ID */
    @Column(name = "room_id")
    private Long roomId;

    /** 회원 ID(users.id) */
    @Column(name = "user_id")
    private Long userId;

    public DmRoomMemberId(Long roomId, Long userId) {
        this.roomId = roomId;
        this.userId = userId;
    }
}
