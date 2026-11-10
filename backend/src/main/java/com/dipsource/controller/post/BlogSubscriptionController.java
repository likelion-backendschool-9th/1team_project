package com.dipsource.controller.post;

import com.dipsource.dto.post.BlogSubscriptionInfoResponse;
import com.dipsource.dto.post.CancelSubscriptionRequest;
import com.dipsource.dto.post.ChangeSubscriptionPriceRequest;
import com.dipsource.dto.post.MySubscriptionResponse;
import com.dipsource.dto.post.SubscribeRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.post.BlogSubscriptionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 블로그 구독. "나"는 JWT에서만 가져온다.
 * POST /subscriptions·DELETE /subscriptions/me는 딥소스 플러스(plan="plus")와 주소를 같이 쓰는데, 플러스는 아직 없어서 400을 준다.
 */
@RestController
@RequiredArgsConstructor
public class BlogSubscriptionController {

    private final BlogSubscriptionService blogSubscriptionService;

    /** 블로그 구독 (한 달치 결제). 이미 구독 중이어도 204 (멱등) */
    @PostMapping("/subscriptions")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody SubscribeRequest request
    ) {
        rejectPlus(request.plan());
        if (request.authorId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "구독할 블로그를 골라 주세요.");
        }
        blogSubscriptionService.subscribe(requireUserId(principal), request.authorId());
        return ResponseEntity.noContent().build();
    }

    /** 구독 해지. 만료일까지는 계속 읽을 수 있다. 이미 해지했어도 204 (멱등) */
    @DeleteMapping("/subscriptions/me")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CancelSubscriptionRequest request
    ) {
        rejectPlus(request.plan());
        blogSubscriptionService.cancel(requireUserId(principal), request.subscriptionId());
        return ResponseEntity.noContent().build();
    }

    /** 내 구독 목록 (만료 전인 것만) */
    @GetMapping("/subscriptions/me")
    public ApiResponse<List<MySubscriptionResponse>> mine(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.of(blogSubscriptionService.getMine(requireUserId(principal)));
    }

    /** 블로그 주인 화면의 구독료·구독 여부·구독자 수. 비로그인도 볼 수 있다 */
    @GetMapping("/users/{userId}/subscription")
    public ApiResponse<BlogSubscriptionInfoResponse> info(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId
    ) {
        Long viewerId = principal == null ? null : principal.getUserId();
        return ApiResponse.of(blogSubscriptionService.getInfo(userId, viewerId));
    }

    /** 내 블로그 월 구독료(원) 변경. 0이면 구독을 받지 않는다 */
    @PutMapping("/users/me/subscription")
    public ResponseEntity<Void> changePrice(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangeSubscriptionPriceRequest request
    ) {
        blogSubscriptionService.changePrice(requireUserId(principal), request.price());
        return ResponseEntity.noContent().build();
    }

    private static void rejectPlus(String plan) {
        if (plan != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "딥소스 플러스는 아직 준비 중이에요.");
        }
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
