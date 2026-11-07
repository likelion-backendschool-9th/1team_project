package com.dipsource.repository.block;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dipsource.entity.block.Block;
import com.dipsource.entity.block.BlockId;

public interface BlockRepository extends JpaRepository<Block, BlockId>{

    /**
     * 차단 저장. 이미 차단 중이면 무시한다.
     * @return 저장된 행 수 (0이면 이미 차단, 1이면 새로 차단)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT INTO blocks (blocker_id, blocked_id, created_at)
        VALUES (:blockerId, :blockedId, NOW())
        ON CONFLICT (blocker_id, blocked_id) DO NOTHING
        """, nativeQuery = true)
    int insertIgnore(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

    /**
     * 차단 해제. 차단 중이 아니면 아무것도 지우지 않는다.
     * @return 삭제된 행 수 (0이면 원래 차단 아님, 1이면 해제)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        DELETE FROM blocks
        WHERE blocker_id = :blockerId AND blocked_id = :blockedId
        """, nativeQuery = true)
    int deleteByBlockerIdAndBlockedId(
        @Param("blockerId") Long blockerId,
        @Param("blockedId") Long blockedId);
} 