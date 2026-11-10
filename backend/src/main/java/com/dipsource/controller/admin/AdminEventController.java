package com.dipsource.controller.admin;

import com.dipsource.dto.event.EventResponse;
import com.dipsource.dto.event.SaveEventRequest;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.event.EventService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 이벤트 등록·수정·삭제. 권한은 SecurityConfig의 /admin/** 를 따른다. */
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @PostMapping
    public ApiResponse<EventResponse> create(@Valid @RequestBody SaveEventRequest request) {
        return ApiResponse.of(eventService.create(request));
    }

    @PutMapping("/{eventId}")
    public ApiResponse<EventResponse> update(
            @PathVariable Long eventId,
            @Valid @RequestBody SaveEventRequest request
    ) {
        return ApiResponse.of(eventService.update(eventId, request));
    }

    @DeleteMapping("/{eventId}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long eventId) {
        eventService.delete(eventId);
        return ApiResponse.of(Map.of());
    }
}
