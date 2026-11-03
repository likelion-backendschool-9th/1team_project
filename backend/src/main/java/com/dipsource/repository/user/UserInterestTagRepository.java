package com.dipsource.repository.user;

import com.dipsource.entity.user.UserInterestTag;
import com.dipsource.entity.user.UserTagId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** user_interest_tags 테이블 접근. */
public interface UserInterestTagRepository extends JpaRepository<UserInterestTag, UserTagId> {

    /** 사용자가 선택한 관심 태그 ID 목록 */
    @Query("SELECT uit.id.tagId FROM UserInterestTag uit WHERE uit.id.userId = :userId")
    List<Long> findTagIdsByUserId(@Param("userId") Long userId);

    /** 회원의 관심 태그를 모두 지운다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserInterestTag t where t.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
