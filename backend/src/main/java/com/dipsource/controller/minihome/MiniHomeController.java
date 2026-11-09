package com.dipsource.controller.minihome;

import com.dipsource.dto.donation.DonationRequest;
import com.dipsource.dto.minihome.FeaturedDemoRequest;
import com.dipsource.dto.minihome.FeaturedPostRequest;
import com.dipsource.dto.minihome.GuestbookRequest;
import com.dipsource.dto.minihome.MiniHomeCommentRequest;
import com.dipsource.dto.minihome.MiniHomeCommentResponse;
import com.dipsource.dto.minihome.MiniHomeResponse;
import com.dipsource.dto.minihome.RandomMiniHomeResponse;
import com.dipsource.dto.minihome.UpdateMiniHomeRequest;
import com.dipsource.dto.minihome.VisitorStatsResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.donation.DonationService;
import com.dipsource.service.minihome.GuestbookService;
import com.dipsource.service.minihome.MiniHomeCommentService;
import com.dipsource.service.minihome.MiniHomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 미니홈피 조회·수정, 일촌평, 방명록, 후원. */
@RestController
@RequestMapping("/mini-homes")
@RequiredArgsConstructor
public class MiniHomeController {

    private final MiniHomeService miniHomeService;
    private final MiniHomeCommentService miniHomeCommentService;
    private final GuestbookService guestbookService;
    private final DonationService donationService;

    @GetMapping("/random")
    public ApiResponse<RandomMiniHomeResponse> random(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long exclude
    ) {
        return ApiResponse.of(miniHomeService.pickRandom(requireUserId(principal), exclude));
    }

    @GetMapping("/{userId}/visitors")
    public ApiResponse<VisitorStatsResponse> visitors(@PathVariable Long userId) {
        return ApiResponse.of(miniHomeService.getVisitors(userId));
    }

    @PostMapping("/{userId}/visits")
    public ApiResponse<VisitorStatsResponse> visit(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId
    ) {
        return ApiResponse.of(miniHomeService.recordVisit(requireUserId(principal), userId));
    }

    @GetMapping("/{userId}")
    public ApiResponse<MiniHomeResponse> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId
    ) {
        Long viewerId = principal == null ? null : principal.getUserId();
        return ApiResponse.of(miniHomeService.get(userId, viewerId));
    }

    @PutMapping("/me")
    public ApiResponse<MiniHomeResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateMiniHomeRequest request
    ) {
        return ApiResponse.of(miniHomeService.update(requireUserId(principal), request));
    }

    @PutMapping("/me/featured-demo")
    public ApiResponse<MiniHomeResponse> updateFeaturedDemo(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FeaturedDemoRequest request
    ) {
        return ApiResponse.of(miniHomeService.updateFeaturedDemo(requireUserId(principal), request.demoId()));
    }

    @PutMapping("/me/featured-post")
    public ApiResponse<MiniHomeResponse> updateFeaturedPost(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody FeaturedPostRequest request
    ) {
        return ApiResponse.of(miniHomeService.updateFeaturedPost(requireUserId(principal), request.postId()));
    }

    @GetMapping("/{userId}/comments")
    public ApiResponse<PageResponse<MiniHomeCommentResponse>> comments(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.of(miniHomeCommentService.list(userId, pageable));
    }

    @PostMapping("/{userId}/comments")
    public ApiResponse<MiniHomeCommentResponse> writeComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @RequestBody MiniHomeCommentRequest request
    ) {
        return ApiResponse.of(miniHomeCommentService.write(requireUserId(principal), userId, request.content()));
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long commentId
    ) {
        miniHomeCommentService.delete(requireUserId(principal), commentId);
        return ApiResponse.of(null);
    }

    @GetMapping("/{userId}/guestbook")
    public ApiResponse<PageResponse<MiniHomeCommentResponse>> guestbook(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.of(guestbookService.list(requireUserId(principal), userId, pageable));
    }

    @PostMapping("/{userId}/guestbook")
    public ApiResponse<MiniHomeCommentResponse> writeGuestbook(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @RequestBody GuestbookRequest request
    ) {
        return ApiResponse.of(guestbookService.write(
                requireUserId(principal), userId, request.content(), request.secret()));
    }

    @DeleteMapping("/guestbook/{guestbookId}")
    public ApiResponse<Void> deleteGuestbook(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long guestbookId
    ) {
        guestbookService.delete(requireUserId(principal), guestbookId);
        return ApiResponse.of(null);
    }

    /** 미니홈피 후원 (로그인 필수, 본인 불가) */
    @PostMapping("/{userId}/donations")
    public ResponseEntity<Void> donate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @Valid @RequestBody DonationRequest request
    ) {
        donationService.donate(requireUserId(principal), userId, request.chips());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
