package com.dipsource.entity.dm;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** DM 대화방 참여자. 테이블: dm_room_members */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "dm_room_members",
        indexes = @Index(name = "idx_dm_room_members_user", columnList = "user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DmRoomMember {

    /** 대화방 ID·회원 ID 복합 키 */
    @EmbeddedId
    private DmRoomMemberId id;

    /** 참여한 대화방 */
    @MapsId("roomId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private DmRoom room;

    /** 마지막으로 읽은 메시지 ID(dm_messages.id). 안 읽은 메시지 수 계산에 쓴다 */
    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    /** 참여 시각 */
    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    /** 나간 시각. 나가지 않았으면 null */
    @Column(name = "left_at")
    private LocalDateTime leftAt;

    public DmRoomMember(DmRoom room, Long userId) {
        this.id = new DmRoomMemberId(room.getId(), userId);
        this.room = room;
    }

    public void read(Long messageId) {
        this.lastReadMessageId = messageId;
    }

    public void leave() {
        this.leftAt = LocalDateTime.now();
    }

    public void rejoin() {
        this.leftAt = null;
    }
}
