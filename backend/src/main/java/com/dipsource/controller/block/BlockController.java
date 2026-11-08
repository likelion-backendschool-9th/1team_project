package com.dipsource.controller.block;

import com.dipsource.dto.block.BlockedUserResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.block.BlockService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 차단·해제. "나"는 JWT에서만 가져온다. */
@RestController
@RequestMapping("/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    /** 내가 차단한 회원 목록. */
    @GetMapping
    public ApiResponse<List<BlockedUserResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.of(blockService.listBlocked(requireUserId(principal)));
    }

    /** 차단. 이미 차단 중이어도 204 (멱등). */
    @PostMapping("/{userId}")
    public ResponseEntity<Void> block(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId
    ) {
        blockService.block(requireUserId(principal), userId);
        return ResponseEntity.noContent().build();
    }

    /** 차단 해제. 차단 중이 아니어도 204 (멱등). */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unblock(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId
    ) {
        blockService.unblock(requireUserId(principal), userId);
        return ResponseEntity.noContent().build();
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}