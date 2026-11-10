package com.dipsource.entity.notification;

/** 알림 종류 */
public enum NotificationType {
    /** 내 글·데모에 좋아요 */
    LIKE,
    /** 내 글·데모에 댓글 */
    COMMENT,
    /** 나를 팔로우 */
    FOLLOW,
    /** 내 미니홈피에 방명록 */
    GUESTBOOK,
    /** 내 피드를 리포스트 */
    REPOST,
    /** 내 블로그를 구독 */
    SUBSCRIBE,
    /** 내 미니홈피에 후원 */
    SPONSOR,
    /** 내 시리즈를 구매 */
    SERIES_PURCHASE
}
