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
    SERIES_PURCHASE,
    /** 내 블로그 글을 북마크 */
    BOOKMARK,
    /** 내 댓글에 답글 */
    REPLY,
    /** 내가 시리즈를 샀다 (구매 완료 안내. actor는 시리즈 작가) */
    PURCHASE_COMPLETE,
    /** 내가 산 시리즈에 새 회차가 올라왔다 (actor는 시리즈 작가) */
    NEW_EPISODE
}
