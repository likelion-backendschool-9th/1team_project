package com.dipsource.service.news.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NewsCategoriesTest {

    @Test
    void prefersTitleOverAiMentionInSummary() {
        String category = NewsCategories.classify(
                "Spring Boot 3.5 릴리스 노트 정리",
                "AI 코드 리뷰 도구와 LLM 에이전트 연동 사례도 함께 소개한다.");

        assertThat(category).isEqualTo("backend");
    }
}
