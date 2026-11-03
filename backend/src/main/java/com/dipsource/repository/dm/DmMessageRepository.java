package com.dipsource.repository.dm;

import com.dipsource.entity.dm.DmMessage;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** dm_messages 테이블 접근. */
public interface DmMessageRepository extends JpaRepository<DmMessage, Long> {

    /** 대화방의 메시지(삭제 제외). 정렬은 pageable로 정한다. */
    Page<DmMessage> findByRoomIdAndDeletedAtIsNull(Long roomId, Pageable pageable);

    /** 대화방의 가장 최근 메시지(삭제 제외) */
    Optional<DmMessage> findFirstByRoomIdAndDeletedAtIsNullOrderByIdDesc(Long roomId);

    /** 상대가 보낸 메시지 중 lastReadId 이후 것(안 읽은 메시지) 수 */
    long countByRoomIdAndSenderIdNotAndIdGreaterThanAndDeletedAtIsNull(Long roomId, Long myId, Long lastReadId);
}
