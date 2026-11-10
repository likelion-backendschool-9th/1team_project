package com.dipsource.repository.block;

import com.dipsource.entity.block.Block;
import com.dipsource.entity.block.BlockId;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** 검색에서 쓰는 차단 관계 조회. */
public interface BlockSearchRepository extends Repository<Block, BlockId> {

    /** 내가 차단했거나 나를 차단한 회원. 양쪽 다 서로의 콘텐츠를 보지 않게 한다 */
    @Query(nativeQuery = true, value = """
            SELECT blocked_id FROM blocks WHERE blocker_id = :userId
            UNION
            SELECT blocker_id FROM blocks WHERE blocked_id = :userId
            """)
    List<Long> findBlockRelatedUserIds(@Param("userId") Long userId);
}
