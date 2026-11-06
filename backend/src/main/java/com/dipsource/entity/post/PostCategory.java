package com.dipsource.entity.post;

import java.util.Arrays;
import java.util.Optional;

/** 블로그 글 분류. 프론트는 소문자 코드(frontend, backend …)로 주고받는다. */
public enum PostCategory {
    FRONTEND("frontend"),
    BACKEND("backend"),
    AI("ai"),
    MOBILE("mobile"),
    CAREER("career"),
    GAME("game"),
    UXUI("uxui");

    /** 프론트와 주고받는 코드 */
    private final String code;

    PostCategory(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    /** 코드로 분류를 찾는다. 없는 코드면 빈 값. */
    public static Optional<PostCategory> fromCode(String code) {
        return Arrays.stream(values()).filter(category -> category.code.equals(code)).findFirst();
    }
}
