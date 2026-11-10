package com.dipsource.controller.report;

import com.dipsource.dto.report.ReportReasonRequest;
import com.dipsource.dto.report.ReportResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.report.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 신고 컨트롤러
 *
 * <p>모든 엔드포인트는 로그인 필수, 201 Created 반환
 * 신고자 ID는 JWT에서 추출 — 요청 본문에 포함하지 않음
 *
 * <pre>
 * POST /feeds/{feedId}/report
 * POST /posts/{postId}/report
 * POST /demos/{demoId}/report
 * POST /feeds/comments/{commentId}/report
 * POST /mini-homes/{homeUserId}/guestbook/{guestbookId}/report
 * POST /users/{targetUserId}/report
 * </pre>
 */
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** 피드 신고 */
    @PostMapping("/feeds/{feedId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportFeed(
            @PathVariable Long feedId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "FEED", feedId, body.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 블로그 글 신고 */
    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "POST", postId, body.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 데모 신고 */
    @PostMapping("/demos/{demoId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportDemo(
            @PathVariable Long demoId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "DEMO", demoId, body.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 피드 댓글 신고 */
    @PostMapping("/feeds/comments/{commentId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportFeedComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "FEED_COMMENT", commentId, body.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /**
     * 방명록 신고
     *
     * <p>{@code homeUserId}는 방명록이 올바른 미니홈피에 속하는지 검증에 사용
     * 예) /mini-homes/5/guestbook/42/report → 42번 방명록의 미니홈피 주인이 실제로 5번 유저인지 확인
     */
    @PostMapping("/mini-homes/{homeUserId}/guestbook/{guestbookId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportGuestbook(
            @PathVariable Long homeUserId,
            @PathVariable Long guestbookId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.createGuestbookReport(
                principal.getUserId(), guestbookId, homeUserId, body.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 유저 신고 */
    @PostMapping("/users/{targetUserId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportUser(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "USER", targetUserId, body.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }
}
