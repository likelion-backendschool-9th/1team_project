package com.dipsource.repository.post;

import com.dipsource.entity.post.PostPurchase;
import com.dipsource.entity.post.PostUserId;
import org.springframework.data.jpa.repository.JpaRepository;

/** post_purchases 테이블 접근 */
public interface PostPurchaseRepository extends JpaRepository<PostPurchase, PostUserId> {
}
