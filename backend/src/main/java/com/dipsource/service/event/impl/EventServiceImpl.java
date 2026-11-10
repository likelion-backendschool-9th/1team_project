package com.dipsource.service.event.impl;

import com.dipsource.dto.event.EventResponse;
import com.dipsource.dto.event.SaveEventRequest;
import com.dipsource.entity.event.Event;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.event.EventRepository;
import com.dipsource.service.event.EventService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventServiceImpl implements EventService {

    static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_EMOJI = "🎉";
    private static final String CTA_LABEL = "데모 둘러보기";
    private static final String CTA_HREF = "/demos";
    private static final Set<String> STATUSES = Set.of("ongoing", "upcoming", "ended");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final EventRepository eventRepository;
    private final Clock clock;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this(eventRepository, Clock.system(ZONE));
    }

    EventServiceImpl(EventRepository eventRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> list(String status, Pageable pageable) {
        String filter = normalizeStatus(status);
        LocalDate today = LocalDate.now(clock);
        return PageResponse.of(
                eventRepository.findVisible(filter, today, page(pageable)),
                event -> toResponse(event, today));
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse get(Long eventId) {
        return toResponse(findActive(eventId), LocalDate.now(clock));
    }

    @Override
    @Transactional
    public EventResponse create(SaveEventRequest request) {
        checkPeriod(request.startAt(), request.endAt());
        Event event = Event.create(
                request.title().trim(),
                emojiOrDefault(request.emoji()),
                request.summary().trim(),
                request.reward().trim(),
                request.startAt(),
                request.endAt());
        return toResponse(eventRepository.save(event), LocalDate.now(clock));
    }

    @Override
    @Transactional
    public EventResponse update(Long eventId, SaveEventRequest request) {
        checkPeriod(request.startAt(), request.endAt());
        Event event = findActive(eventId);
        event.update(
                request.title().trim(),
                emojiOrDefault(request.emoji()),
                request.summary().trim(),
                request.reward().trim(),
                request.startAt(),
                request.endAt());
        return toResponse(event, LocalDate.now(clock));
    }

    @Override
    @Transactional
    public void delete(Long eventId) {
        findActive(eventId).delete();
    }

    private Event findActive(Long eventId) {
        return eventRepository.findByIdAndDeletedAtIsNull(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "이벤트를 찾을 수 없어요."));
    }

    private Pageable page(Pageable pageable) {
        int size = pageable.getPageSize() <= 0 ? DEFAULT_SIZE : Math.min(pageable.getPageSize(), MAX_SIZE);
        int number = Math.max(pageable.getPageNumber(), 0);
        Sort sort = Sort.by(Sort.Order.desc("startsAt"), Sort.Order.desc("id"));
        return PageRequest.of(number, size, sort);
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        if (!STATUSES.contains(status)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "상태 값은 ongoing, upcoming, ended 중 하나예요.");
        }
        return status;
    }

    private static void checkPeriod(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "끝나는 날이 시작일보다 빨라요.");
        }
    }

    private static String emojiOrDefault(String emoji) {
        if (emoji == null || emoji.isBlank()) {
            return DEFAULT_EMOJI;
        }
        return emoji.trim();
    }

    static String statusOf(LocalDate start, LocalDate end, LocalDate today) {
        if (today.isBefore(start)) {
            return "upcoming";
        }
        if (today.isAfter(end)) {
            return "ended";
        }
        return "ongoing";
    }

    private static EventResponse toResponse(Event event, LocalDate today) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getSummary(),
                event.getSummary(),
                statusOf(event.getStartsAt(), event.getEndsAt(), today),
                event.getStartsAt().atStartOfDay(ZONE).format(ISO),
                event.getEndsAt().atTime(23, 59).atZone(ZONE).format(ISO),
                event.getReward(),
                event.getEmoji(),
                0,
                CTA_LABEL,
                CTA_HREF);
    }
}
