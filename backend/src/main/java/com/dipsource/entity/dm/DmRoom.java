package com.dipsource.entity.dm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** DM 대화방. 참여자는 dm_room_members에 있다. 테이블: dm_rooms */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "dm_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DmRoom {

    /** 대화방 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 마지막 메시지 시각. 대화방 목록 정렬에 쓴다. 메시지가 없으면 null */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /** 대화방 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static DmRoom create() {
        return new DmRoom();
    }

    public void touch(LocalDateTime messageAt) {
        this.lastMessageAt = messageAt;
    }
}
