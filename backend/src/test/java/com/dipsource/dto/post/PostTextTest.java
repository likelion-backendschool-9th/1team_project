package com.dipsource.dto.post;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PostTextTest {

    @Test
    void excerptRemovesMarkdownAndCuts() {
        String content = "# 제목\n\n**굵게** 쓴 [링크](https://x.y)와 `코드`\n\n```java\nint a = 1;\n```\n" + "가".repeat(200);

        String excerpt = PostText.excerpt(content);

        assertThat(excerpt).startsWith("제목 굵게 쓴 링크와 코드");
        assertThat(excerpt).doesNotContain("#", "**", "](", "int a");
        assertThat(excerpt).endsWith("…").hasSize(121);
    }

    @Test
    void previewKeepsFirstThreeParagraphs() {
        assertThat(PostText.preview("a\n\nb\n\n  \nc\n\nd")).isEqualTo("a\n\nb\n\nc");
    }

    @Test
    void readMinutesIsAtLeastOne() {
        assertThat(PostText.readMinutes("짧음")).isEqualTo(1);
        assertThat(PostText.readMinutes("가".repeat(1001))).isEqualTo(3);
    }
}
