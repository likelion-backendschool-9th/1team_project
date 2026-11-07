package com.dipsource.dto.minihome;

/** 대표 글(제작기 글) 변경 요청. postId가 null이면 연결을 끊는다. */
public record FeaturedPostRequest(
        Long postId
) {
}
