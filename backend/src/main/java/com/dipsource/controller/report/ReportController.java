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
 * POST /feeds/{feedId}/report                                   — 피드 신고
 * POST /posts/{postId}/report                                   — 블로그 글 신고
 * POST /demos/{demoId}/report                                   — 데모 신고
 * POST /feeds/comments/{commentId}/report                       — 피드 댓글 신고
 * POST /mini-homes/{homeUserId}/guestbook/{guestbookId}/report  — 방명록 신고
 * POST /users/{targetUserId}/report                             — 유저 신고
 */
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** 피드 신고. 201 Created 반환 */
    @PostMapping("/feeds/{feedId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportFeed(
            @PathVariable Long feedId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "FEED", feedId, body.reason(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 블로그 글 신고. 201 Created 반환 */
    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "POST", postId, body.reason(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 데모 신고. 201 Created 반환 */
    @PostMapping("/demos/{demoId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportDemo(
            @PathVariable Long demoId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "DEMO", demoId, body.reason(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 피드 댓글 신고. 201 Created 반환 */
    @PostMapping("/feeds/comments/{commentId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportFeedComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "FEED_COMMENT", commentId, body.reason(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 방명록 신고. 201 Created 반환 */
    @PostMapping("/mini-homes/{homeUserId}/guestbook/{guestbookId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportGuestbook(
            @PathVariable Long homeUserId,
            @PathVariable Long guestbookId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "GUESTBOOK", guestbookId, body.reason(), homeUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 유저 신고. 201 Created 반환 */
    @PostMapping("/users/{targetUserId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> reportUser(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportReasonRequest body
    ) {
        ReportResponse response = reportService.create(principal.getUserId(), "USER", targetUserId, body.reason(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }
}
