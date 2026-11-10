package com.dipsource.dto.post;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.Post;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 글 상세 (프론트 PostDetail = PostSummary + 본문).
 *
 * @param content     본문. 잠긴 유료 글이면 앞 3문단만
 * @param tags        태그 이름
 * @param purchased   본문 전체를 볼 수 있는지 (작성자·구매자·시리즈 구매자)
 * @param seriesTitle 들어 있는 시리즈 제목. 없으면 null
 * @param bookmarked  내가 북마크했는지 (비로그인이면 false)
 * @param paid        잠긴 글인지 (시리즈 안 유료 글은 price가 0이어도 true)
 * @param seriesId    들어 있는 시리즈. 없으면 null
 * @param seriesOrder 시리즈 안 회차 번호. 없으면 null
 */
public record PostDetailResponse(
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
        boolean liked,
        String content,
        List<String> tags,
        boolean purchased,
        String seriesTitle,
        int viewCount,
        boolean bookmarked,
        boolean paid,
        Long seriesId,
        Integer seriesOrder
) {

    public static PostDetailResponse of(
            Post post, UserSummaryResponse author, List<String> tags, boolean unlocked, boolean liked, boolean bookmarked,
            String seriesTitle) {
        PostSummaryResponse summary = PostSummaryResponse.of(post, author, liked);
        return new PostDetailResponse(
                summary.id(),
                summary.author(),
                summary.title(),
                summary.excerpt(),
                summary.category(),
                summary.createdAt(),
                summary.price(),
                summary.readMinutes(),
                summary.demo(),
                summary.likeCount(),
                summary.commentCount(),
                summary.repostCount(),
                summary.liked(),
                unlocked ? post.getContent() : PostText.preview(post.getContent()),
                tags,
                unlocked,
                seriesTitle,
                post.getViewCount(),
                bookmarked,
                post.isPaid(),
                post.getSeriesId(),
                post.getSeriesOrder()
        );
    }
}
