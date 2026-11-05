package com.dipsource.dto.minihome;

/** 방명록 작성 요청. secret이 없으면 공개글이다. */
public record GuestbookRequest(String content, Boolean secret) {
}
