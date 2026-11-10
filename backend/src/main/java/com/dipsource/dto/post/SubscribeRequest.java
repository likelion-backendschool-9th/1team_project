package com.dipsource.dto.post;

/**
 * 구독 요청. 블로그 구독이면 authorId, 딥소스 플러스면 plan="plus".
 *
 * @param authorId 구독할 블로그 주인
 * @param plan     "plus"면 딥소스 플러스
 */
public record SubscribeRequest(Long authorId, String plan) {
}
