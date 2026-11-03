package com.dipsource.global.common;

/** 성공 응답 공통 래퍼. { "data": ... } 형식. */
public record ApiResponse<T>(T data) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }
}
