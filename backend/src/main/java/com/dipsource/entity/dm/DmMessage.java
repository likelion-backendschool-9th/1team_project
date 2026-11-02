package com.dipsource.entity.dm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** DM 메시지. 테이블: dm_messages */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "dm_messages",
        indexes = @Index(name = "idx_dm_messages_room_created", columnList = "room_id, created_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DmMessage {

    /** 메시지 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 메시지가 속한 대화방 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private DmRoom room;

    /** 보낸 회원 ID(users.id) */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /** 내용 */
    @Column(nullable = false, length = 1000)
    private String content;

    /** 보낸 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 삭제 시각. 삭제하지 않았으면 null */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private DmMessage(DmRoom room, Long senderId, String content) {
        this.room = room;
        this.senderId = senderId;
        this.content = content;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
