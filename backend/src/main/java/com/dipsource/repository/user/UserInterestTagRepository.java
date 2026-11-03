package com.dipsource.repository.user;

import com.dipsource.entity.user.UserInterestTag;
import com.dipsource.entity.user.UserTagId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserInterestTagRepository extends JpaRepository<UserInterestTag, UserTagId> {

    /** 사용자가 선택한 관심 태그 ID 목록 */
    @Query("SELECT uit.id.tagId FROM UserInterestTag uit WHERE uit.id.userId = :userId")
    List<Long> findTagIdsByUserId(@Param("userId") Long userId);
}
