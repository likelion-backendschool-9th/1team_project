package com.dipsource.dto.post;

/**
 * 블로그 주인 화면에 보여 줄 통계.
 *
 * @param bio        회원 소개. 없으면 빈 문자열
 * @param postCount  공개 중인 글 수
 * @param totalViews 공개 중인 글의 조회수 합
 */
public record BlogStatsResponse(String bio, long postCount, long totalViews) {
}
