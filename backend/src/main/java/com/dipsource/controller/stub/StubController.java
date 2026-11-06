package com.dipsource.controller.stub;

import com.dipsource.global.common.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 미구현 API 스텁 — 프론트 페이지 진입 시 에러 방지용
 * 실제 구현이 완료되면 해당 엔드포인트를 여기서 제거하고 전용 컨트롤러로 이관
 */
@RestController
@RequestMapping("/api/v1")
public class StubController {

    private static final Map<String, Object> EMPTY_PAGE = Map.of(
            "content", List.of(),
            "page", 0,
            "size", 0,
            "totalElements", 0,
            "totalPages", 0
    );

    // ── 이벤트 ──

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<Map<String, Object>>> events() {
        return ok(EMPTY_PAGE);
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<Object>> eventDetail(@PathVariable Long eventId) {
        return notFound();
    }

    // ── 랭킹 ──

    @GetMapping("/rankings")
    public ResponseEntity<ApiResponse<List<Object>>> rankings() {
        return ok(List.of());
    }

    // ── 알림 ──

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<Map<String, Object>>> notifications() {
        return ok(EMPTY_PAGE);
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unreadCount() {
        return ok(Map.of("count", 0));
    }

    // ── 코인 ──

    @GetMapping("/coins/history")
    public ResponseEntity<ApiResponse<Map<String, Object>>> coinHistory() {
        return ok(EMPTY_PAGE);
    }

    // ── 북마크 ──

    @GetMapping("/users/me/bookmarks")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bookmarks() {
        return ok(EMPTY_PAGE);
    }

    // ── 검색 (인기 키워드) ──

    @GetMapping("/search/popular")
    public ResponseEntity<ApiResponse<List<Object>>> popularKeywords() {
        return ok(List.of());
    }

    // ── 유틸 ──

    private <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.of(data));
    }

    private <T> ResponseEntity<ApiResponse<T>> notFound() {
        return ResponseEntity.notFound().build();
    }
}
