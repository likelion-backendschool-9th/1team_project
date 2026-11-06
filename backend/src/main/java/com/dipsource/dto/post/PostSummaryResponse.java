package com.dipsource.dto.post;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.Post;
import java.time.LocalDateTime;

/**
 * 블로그 목록의 글 한 건 (프론트 PostSummary).
 *
 * @param excerpt     본문 앞부분 요약. 유료 글은 잠금 화면과 같은 앞 3문단 안에서만 만든다
 * @param category    분류 코드
 * @param price       0이면 무료, 양수면 잠금 해제 칩
 * @param readMinutes 읽는 데 걸리는 분 (본문 길이로 계산)
 * @param demo        연결한 데모. 데모 조회가 생기기 전까지 null
 * @param repostCount 블로그 리포스트는 아직 없어서 0
 * @param liked       내가 좋아요했는지. 좋아요 기능 전이라 false
 */
public record PostSummaryResponse(
        Long id,
        UserSummaryResponse author,
        String title,
        String excerpt,
        String category,
        LocalDateTime createdAt,
        int price,
        int readMinutes,
        Object demo,
        int likeCount,
        int commentCount,
        int repostCount,
        boolean liked
) {

    public static PostSummaryResponse of(Post post, UserSummaryResponse author) {
        return new PostSummaryResponse(
                post.getId(),
                author,
                post.getTitle(),
                // 유료 글은 잠긴 문단이 요약으로 새지 않게 미리보기(앞 3문단) 안에서만 자른다.
                PostText.excerpt(post.isPaid() ? PostText.preview(post.getContent()) : post.getContent()),
                post.getCategory().code(),
                post.getCreatedAt(),
                post.isPaid() ? post.getPriceChips() : 0,
                PostText.readMinutes(post.getContent()),
                null,
                post.getLikeCount(),
                post.getCommentCount(),
                0,
                false
        );
    }
}
