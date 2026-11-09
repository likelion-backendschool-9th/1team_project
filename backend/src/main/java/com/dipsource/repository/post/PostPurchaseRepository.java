package com.dipsource.repository.post;

import com.dipsource.entity.post.PostPurchase;
import com.dipsource.entity.post.PostUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** post_purchases 테이블 접근 */
public interface PostPurchaseRepository extends JpaRepository<PostPurchase, PostUserId> {

    /**
     * 구매 기록 저장. 이미 산 글이면 무시한다.
     * 같은 구매가 동시에 들어오면 두 번째 요청은 첫 요청이 끝날 때까지 기다렸다가 0을 받는다.
     * @return 0이면 이미 구매, 1이면 새로 구매
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO post_purchases (user_id, post_id, paid_chips, purchased_at)
            VALUES (:userId, :postId, :paidChips, NOW())
            ON CONFLICT (user_id, post_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("postId") Long postId, @Param("paidChips") int paidChips);
}
