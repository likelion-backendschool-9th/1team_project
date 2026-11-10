package com.dipsource.service.event.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.event.EventResponse;
import com.dipsource.dto.event.SaveEventRequest;
import com.dipsource.entity.event.Event;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.event.EventRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 10);
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Mock
    private EventRepository eventRepository;
    private EventServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EventServiceImpl(eventRepository, Clock.fixed(TODAY.atStartOfDay(ZONE).toInstant(), ZONE));
    }

    @Test
    void 시작일_전이면_예정이고_시작일과_종료일_당일은_진행_중이며_종료일_다음날은_종료다() {
        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(create(TODAY.plusDays(1), TODAY.plusDays(2)).status()).isEqualTo("upcoming");
        assertThat(create(TODAY, TODAY).status()).isEqualTo("ongoing");
        assertThat(create(TODAY.minusDays(3), TODAY).status()).isEqualTo("ongoing");
        EventResponse ended = create(TODAY.minusDays(5), TODAY.minusDays(1));
        assertThat(ended.status()).isEqualTo("ended");
        assertThat(ended.emoji()).isEqualTo("🎉");
        assertThat(ended.participantCount()).isZero();
        assertThat(ended.content()).isEqualTo(ended.summary());
        assertThat(ended.ctaLabel()).isEqualTo("데모 둘러보기");
        assertThat(ended.ctaHref()).isEqualTo("/demos");
        assertThat(ended.startAt()).isEqualTo("2026-11-05T00:00:00+09:00");
        assertThat(ended.endAt()).isEqualTo("2026-11-09T23:59:00+09:00");
    }

    @Test
    void 종료일이_시작일보다_빠르면_400이다() {
        SaveEventRequest request = request(TODAY, TODAY.minusDays(1));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void 삭제된_이벤트는_404이다() {
        when(eventRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(7L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void 목록은_요청한_상태로_거른다() {
        Event ongoing = Event.create("진행", "🎉", "소개", "보상", TODAY.minusDays(1), TODAY.plusDays(1));
        ReflectionTestUtils.setField(ongoing, "id", 3L);
        when(eventRepository.findVisible(eq("ongoing"), eq(TODAY), any()))
                .thenReturn(new PageImpl<>(List.of(ongoing), PageRequest.of(0, 20), 1));

        PageResponse<EventResponse> page = service.list("ongoing", PageRequest.of(0, 20));

        assertThat(page.content()).extracting(EventResponse::status).containsExactly("ongoing");
        assertThat(page.content()).extracting(EventResponse::id).containsExactly(3L);
        assertThat(page.totalElements()).isEqualTo(1);
        ArgumentCaptor<LocalDate> today = ArgumentCaptor.forClass(LocalDate.class);
        verify(eventRepository).findVisible(eq("ongoing"), today.capture(), any());
        assertThat(today.getValue()).isEqualTo(TODAY);
    }

    private EventResponse create(LocalDate start, LocalDate end) {
        return service.create(request(start, end));
    }

    private static SaveEventRequest request(LocalDate start, LocalDate end) {
        return new SaveEventRequest("제목", " ", "소개", "보상", start, end, "upcoming");
    }
}
