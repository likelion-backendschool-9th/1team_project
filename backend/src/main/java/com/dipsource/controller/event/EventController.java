package com.dipsource.controller.event;

import com.dipsource.dto.event.EventResponse;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.service.event.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 이벤트 조회. 비로그인도 볼 수 있다. */
@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping("/events")
    public ApiResponse<PageResponse<EventResponse>> events(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.of(eventService.list(status, pageable));
    }

    @GetMapping("/events/{eventId}")
    public ApiResponse<EventResponse> event(@PathVariable Long eventId) {
        return ApiResponse.of(eventService.get(eventId));
    }
}
