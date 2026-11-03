package com.dipsource.service.news.impl;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** 제목·요약 키워드로 분야를 정한다. 먼저 맞는 분야를 쓴다. */
final class NewsCategories {

    static final String ETC = "etc";

    private static final List<String> SECURITY = List.of("보안", "취약점", "해킹", "cve", "xss", "csrf", "openssl");
    private static final List<String> AI = List.of(
            "인공지능", "머신러닝", "딥러닝", "machine learning", "deep learning", "openai", "chatgpt", "llm", "claude", "gemini");
    private static final List<String> GAME = List.of("게임", "unity", "unreal", "webgpu");
    private static final List<String> FRONTEND = List.of(
            "프론트엔드", "프론트", "react", "vue", "angular", "next.js", "nextjs", "svelte", "typescript", "javascript", "자바스크립트");
    private static final List<String> BACKEND = List.of(
            "백엔드", "spring", "kotlin", "django", "postgres", "mysql", "redis", "kafka", "golang", "node.js", "nestjs");

    private static final Pattern WORD_SECURITY = Pattern.compile("\\bsecurity\\b");
    private static final Pattern WORD_AI = Pattern.compile("\\bai\\b");
    private static final Pattern WORD_GAME = Pattern.compile("\\bgame\\b");
    private static final Pattern WORD_JAVA = Pattern.compile("\\bjava\\b");
    private static final Pattern WORD_GO = Pattern.compile("\\bgo\\b");
    private static final Pattern WORD_CSS = Pattern.compile("\\bcss\\b");
    private static final Pattern WORD_HTML = Pattern.compile("\\bhtml\\b");

    private NewsCategories() {
    }

    static String classify(String title, String summary) {
        String text = (title + " " + summary).toLowerCase(Locale.ROOT);
        if (containsAny(text, SECURITY) || WORD_SECURITY.matcher(text).find()) {
            return "security";
        }
        if (containsAny(text, AI) || WORD_AI.matcher(text).find()) {
            return "ai";
        }
        if (containsAny(text, GAME) || WORD_GAME.matcher(text).find()) {
            return "game";
        }
        if (containsAny(text, FRONTEND) || WORD_CSS.matcher(text).find() || WORD_HTML.matcher(text).find()) {
            return "frontend";
        }
        if (containsAny(text, BACKEND) || WORD_JAVA.matcher(text).find() || WORD_GO.matcher(text).find()) {
            return "backend";
        }
        return ETC;
    }

    private static boolean containsAny(String text, List<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
