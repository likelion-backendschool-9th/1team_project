package com.dipsource.dto.post;

import static org.assertj.core.api.Assertions.assertThat;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostCategory;
import java.util.List;
import org.junit.jupiter.api.Test;

class PostSummaryResponseTest {

    /** 앞 3문단이 120자보다 짧아서, 그냥 자르면 잠긴 4문단까지 요약에 들어가는 본문 */
    private static final String SHORT_OPENING = "안녕하세요.\n\n오늘은 회고입니다.\n\n시작할게요.\n\n핵심 비법은 잠겨 있어야 합니다.";

    private static final UserSummaryResponse AUTHOR = new UserSummaryResponse(1L, "작성자", null, List.of());

    @Test
    void paidPostExcerptStaysInsidePreview() {
        String excerpt = PostSummaryResponse.of(post(true), AUTHOR, false).excerpt();

        assertThat(excerpt).isEqualTo("안녕하세요. 오늘은 회고입니다. 시작할게요.");
        assertThat(excerpt).doesNotContain("핵심 비법");
    }

    @Test
    void freePostExcerptUsesWholeContent() {
        assertThat(PostSummaryResponse.of(post(false), AUTHOR, false).excerpt()).contains("핵심 비법");
    }

    private static Post post(boolean paid) {
        return Post.builder().userId(1L).title("회고").category(PostCategory.CAREER)
                .content(SHORT_OPENING).paid(paid).priceChips(paid ? 30 : 0).build();
    }
}
