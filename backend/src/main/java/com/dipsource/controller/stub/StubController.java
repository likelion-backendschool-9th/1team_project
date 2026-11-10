package com.dipsource.controller.stub;

import com.dipsource.global.common.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 미구현 API 스텁 — 프론트 페이지 진입 시 에러 방지용
 * 실제 구현이 완료되면 해당 엔드포인트를 여기서 제거하고 전용 컨트롤러로 이관
 */
@RestController
@RequestMapping("")
public class StubController {

    // 이벤트(GET /events)는 EventController로 이관했다.
    // 랭킹(GET /rankings)은 RankingController로 이관했다.
    // 북마크(GET /users/me/bookmarks)는 MyBookmarkController로 이관했다.

    // ── 검색 (인기 키워드) ──

    @GetMapping("/search/popular")
    public ResponseEntity<ApiResponse<List<Object>>> popularKeywords() {
        return ok(List.of());
    }

    // ── 유틸 ──

    private <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.of(data));
    }
}
