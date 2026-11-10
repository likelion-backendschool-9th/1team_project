package com.dipsource.global.exception;

/** 에러 응답 본문. { "error": { "code", "message" } } */
public record ErrorResponse(ErrorBody error) {

    public record ErrorBody(String code, String message) {
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(new ErrorBody(code, message));
    }
}
