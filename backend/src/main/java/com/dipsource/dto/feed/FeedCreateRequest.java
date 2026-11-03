package com.dipsource.dto.feed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 피드 생성 요청 DTO
 * POST /api/v1/feeds 에서 사용
 */
public record FeedCreateRequest(

        /** 피드 본문. 필수, 최대 500자 */
        @NotBlank @Size(max = 500) String content,

        /** 첨부할 데모 ID(demos.id). 데모를 첨부하지 않으면 null. ERD의 feeds.demo_id에 대응 */
        Long demoId,

        /** 피드에 달 태그 ID 목록. 태그가 없으면 null 또는 빈 리스트 */
        List<Long> tagIds,

        /** 투표 선택지. 투표가 없는 피드면 null. 예: ["Spring", "Node"] */
        List<String> pollOptions
) {}
