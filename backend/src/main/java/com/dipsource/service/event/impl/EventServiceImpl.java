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
import java.util.regex.Pattern;
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
    private static final String CTA_LABEL = "이벤트 참여하기";
    /** 참여 경로를 정하지 않은 이벤트의 기본 경로 */
    private static final String DEFAULT_CTA_HREF = "/demos";
    /** 사이트 안 경로만 받는다. //로 시작하면 다른 사이트로 가므로 막는다 */
    private static final Pattern INTERNAL_PATH = Pattern.compile("^/(?![/\\\\])[^\\s\\\\]*$");
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
                request.endAt(),
                imageUrlOrNull(request.imageUrl()),
                ctaHrefOrNull(request.ctaHref()));
        return toResponse(eventRepository.save(event), LocalDate.now(clock));
    }

    @Override
    @Transactional
    public EventResponse update(Long eventId, SaveEventRequest request) {
        checkPeriod(request.startAt(), request.endAt());
        String imageUrl = imageUrlOrNull(request.imageUrl());
        String ctaHref = ctaHrefOrNull(request.ctaHref());
        Event event = findActive(eventId);
        event.update(
                request.title().trim(),
                emojiOrDefault(request.emoji()),
                request.summary().trim(),
                request.reward().trim(),
                request.startAt(),
                request.endAt(),
                imageUrl,
                ctaHref);
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

    private static String imageUrlOrNull(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        String url = imageUrl.trim();
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미지 주소를 확인해 주세요.");
        }
        return url;
    }

    private static String ctaHrefOrNull(String ctaHref) {
        if (ctaHref == null || ctaHref.isBlank()) {
            return null;
        }
        String href = ctaHref.trim();
        if (!INTERNAL_PATH.matcher(href).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "참여 경로는 /demos/upload처럼 /로 시작하는 사이트 안 주소여야 해요.");
        }
        return href;
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
                event.getCtaHref() == null ? DEFAULT_CTA_HREF : event.getCtaHref(),
                event.getImageUrl());
    }
}
