package com.dipsource.entity.post;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** (user_id, comment_id) 복합 PK */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostCommentLikeId implements Serializable {

    /** 회원 ID(users.id) */
    @Column(name = "user_id")
    private Long userId;

    /** 블로그 댓글 ID */
    @Column(name = "comment_id")
    private Long commentId;

    public PostCommentLikeId(Long userId, Long commentId) {
        this.userId = userId;
        this.commentId = commentId;
    }
}
