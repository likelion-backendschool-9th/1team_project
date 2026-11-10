package com.dipsource.dto.event;

/**
 * 사용자·관리자 화면이 같이 쓰는 이벤트 응답. status는 저장값이 아니라 날짜로 계산한 값이다.
 * imageUrl은 없으면 null, ctaHref는 저장한 참여 경로가 없으면 데모 목록이다.
 */
public record EventResponse(
        Long id,
        String title,
        String summary,
        String content,
        String status,
        String startAt,
        String endAt,
        String reward,
        String emoji,
        int participantCount,
        String ctaLabel,
        String ctaHref,
        String imageUrl
) {
}
