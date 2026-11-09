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
 * @param purchased   본문 전체를 볼 수 있는지 (작성자·구매자·구독자)
 * @param seriesTitle 시리즈는 아직 없어서 null
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
        int viewCount
) {

    public static PostDetailResponse of(Post post, UserSummaryResponse author, List<String> tags, boolean unlocked, boolean liked) {
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
                null,
                post.getViewCount()
        );
    }
}
