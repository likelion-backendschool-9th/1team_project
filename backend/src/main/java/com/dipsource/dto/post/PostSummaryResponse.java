package com.dipsource.dto.post;

import com.dipsource.dto.feed.FeedDemoResponse;
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
 * @param demo        연결한 데모 (피드와 같은 카드 요약). 없거나 숨김·삭제됐으면 null
 * @param repostCount 블로그 리포스트는 아직 없어서 0
 * @param liked       내가 좋아요했는지
 * @param viewCount   조회수 (조회수 순위에 쓴다)
 * @param paid        잠긴 글인지. 시리즈 안 유료 글은 price가 0이어도 true (시리즈를 사야 읽는다)
 * @param seriesId    들어 있는 시리즈. 없으면 null
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
        FeedDemoResponse demo,
        int likeCount,
        int commentCount,
        int repostCount,
        boolean liked,
        int viewCount,
        boolean paid,
        Long seriesId
) {

    public static PostSummaryResponse of(Post post, UserSummaryResponse author) {
        return of(post, author, false, null);
    }

    public static PostSummaryResponse of(Post post, UserSummaryResponse author, boolean liked, FeedDemoResponse demo) {
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
                demo,
                post.getLikeCount(),
                post.getCommentCount(),
                0,
                liked,
                post.getViewCount(),
                post.isPaid(),
                post.getSeriesId()
        );
    }
}
