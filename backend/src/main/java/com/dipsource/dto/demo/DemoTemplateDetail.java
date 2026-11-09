package com.dipsource.dto.demo;

/** 데모 템플릿 내용 */
public record DemoTemplateDetail(
        String key,
        String title,
        String description,
        String usage,
        String html,
        String css,
        String js,
        String mockConfig,
        String sampleData
) {
}
