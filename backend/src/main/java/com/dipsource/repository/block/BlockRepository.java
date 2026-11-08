package com.dipsource.repository.block;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dipsource.entity.block.Block;
import com.dipsource.entity.block.BlockId;

import java.util.List;

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

    /** 내가 차단한 목록. 최근 차단 순. */
    List<Block> findByIdBlockerIdOrderByCreatedAtDesc(Long blockerId);

    /** 나와 상대가 어느 쪽이든 차단했으면 true. */
    @Query("""
            SELECT COUNT(b) > 0 FROM Block b
            WHERE (b.id.blockerId = :leftId AND b.id.blockedId = :rightId)
               OR (b.id.blockerId = :rightId AND b.id.blockedId = :leftId)
            """)
    boolean existsBetween(@Param("leftId") Long leftId, @Param("rightId") Long rightId);

    /** 내가 차단했거나 나를 차단한 회원 ID. */
    @Query("""
            SELECT CASE WHEN b.id.blockerId = :userId THEN b.id.blockedId ELSE b.id.blockerId END
            FROM Block b
            WHERE b.id.blockerId = :userId OR b.id.blockedId = :userId
            """)
    List<Long> findHiddenUserIds(@Param("userId") Long userId);
} 