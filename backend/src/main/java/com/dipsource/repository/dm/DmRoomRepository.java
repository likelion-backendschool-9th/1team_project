package com.dipsource.repository.dm;

import com.dipsource.entity.dm.DmRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** dm_rooms 테이블 접근. */
public interface DmRoomRepository extends JpaRepository<DmRoom, Long> {

    /** 두 회원이 함께 들어 있는 1:1 대화방. 없으면 빈 목록 */
    @Query("""
            select m1.room from DmRoomMember m1, DmRoomMember m2
            where m1.room = m2.room
              and m1.id.userId = :userId
              and m2.id.userId = :partnerId
            """)
    List<DmRoom> findSharedRooms(@Param("userId") Long userId, @Param("partnerId") Long partnerId);

    /**
     * 같은 두 회원의 대화방을 동시에 여러 개 만들지 않도록 트랜잭션이 끝날 때까지 잠근다(PostgreSQL).
     * key는 두 회원 ID로 만든 값이라, 다른 회원 쌍끼리는 서로 기다리지 않는다.
     */
    @Query(value = "select 1 from pg_advisory_xact_lock(:key)", nativeQuery = true)
    Integer lockPair(@Param("key") long key);
}
