package com.dipsource.service.event;

import com.dipsource.dto.event.EventResponse;
import com.dipsource.dto.event.SaveEventRequest;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

public interface EventService {

    PageResponse<EventResponse> list(String status, Pageable pageable);

    EventResponse get(Long eventId);

    EventResponse create(SaveEventRequest request);

    EventResponse update(Long eventId, SaveEventRequest request);

    void delete(Long eventId);
}
