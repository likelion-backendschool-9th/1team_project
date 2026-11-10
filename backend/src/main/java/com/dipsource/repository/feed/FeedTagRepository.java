package com.dipsource.repository.feed;

import com.dipsource.entity.feed.FeedTag;
import com.dipsource.entity.feed.FeedTagId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedTagRepository extends JpaRepository<FeedTag, FeedTagId> {

    /** 특정 피드에 달린 태그 ID 목록 */
    @Query("SELECT ft.id.tagId FROM FeedTag ft WHERE ft.id.feedId = :feedId")
    List<Long> findTagIdsByFeedId(@Param("feedId") Long feedId);

    /** 특정 태그가 달린 피드 ID 목록 */
    @Query("SELECT ft.id.feedId FROM FeedTag ft WHERE ft.id.tagId = :tagId")
    List<Long> findFeedIdsByTagId(@Param("tagId") Long tagId);
}
