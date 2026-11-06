package com.dipsource.repository.post;

import com.dipsource.entity.post.PostTag;
import com.dipsource.entity.post.PostTagId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** post_tags 테이블 접근 */
public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {

    /** 글에 붙은 태그 이름. 이름순 */
    @Query("""
            SELECT t.name FROM PostTag pt JOIN Tag t ON t.id = pt.id.tagId
            WHERE pt.id.postId = :postId
            ORDER BY t.name
            """)
    List<String> findTagNames(@Param("postId") Long postId);

    /** 글의 태그 연결을 모두 지운다 (수정할 때 다시 붙인다). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PostTag pt WHERE pt.id.postId = :postId")
    int deleteByPostId(@Param("postId") Long postId);
}
