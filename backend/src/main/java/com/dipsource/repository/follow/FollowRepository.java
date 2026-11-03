package com.dipsource.repository.follow;

import com.dipsource.entity.follow.Follow;
import com.dipsource.entity.follow.FollowId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    /** 내가 팔로잉하는 사람들의 ID 목록 */
    @Query("SELECT f.id.followingId FROM Follow f WHERE f.id.followerId = :userId")
    List<Long> findFollowingIdsByUserId(@Param("userId") Long userId);
}
