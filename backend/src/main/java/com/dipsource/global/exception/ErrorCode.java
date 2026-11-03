package com.dipsource.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** API 에러 코드와 HTTP 상태. */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "대상을 찾을 수 없습니다"),
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "이미 존재하는 값입니다"),
    NICKNAME_TAKEN(HttpStatus.CONFLICT, "NICKNAME_TAKEN", "이미 사용 중인 닉네임이에요"),
    NEWS_FETCH_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "NEWS_FETCH_FAILED", "뉴스를 불러오지 못했습니다");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;
}
