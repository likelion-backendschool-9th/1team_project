package com.dipsource.dto.search;

import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import java.util.Arrays;

/** 검색 대상. API의 type 파라미터 값과 1:1로 대응한다. */
public enum SearchType {
    ALL("all"),
    POST("post"),
    FEED("feed"),
    DEMO("demo"),
    USER("user");

    private final String value;

    SearchType(String value) {
        this.value = value;
    }

    /** type을 보내지 않으면 전체 검색이다. */
    public static SearchType from(String value) {
        if (value == null) {
            return ALL;
        }
        return Arrays.stream(values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST,
                        "지원하지 않는 검색 대상이에요. type은 all, post, feed, demo, user 중 하나예요"));
    }
}
