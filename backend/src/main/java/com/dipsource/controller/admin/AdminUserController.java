package com.dipsource.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dipsource.dto.admin.AdminUserDetailResponse;
import com.dipsource.dto.admin.AdminUserResponse;
import com.dipsource.dto.admin.BanUserRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.admin.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@RequestMapping("/admin/users")
@RequiredArgsConstructor 
public class AdminUserController {

    private final AdminUserService adminUserService;
    
    /** 회원 목록. ?q=&status=&page=&size= */
    @GetMapping
    public ApiResponse<PageResponse<AdminUserResponse>> listUsers(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String status,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        return ApiResponse.of(adminUserService.listUsers(q, status, pageable));
    }

    /** 회원 상세 + 제재 이력. */
    @GetMapping("/{userId}")
    public ApiResponse<AdminUserDetailResponse> getUser(@PathVariable Long userId) {
        return ApiResponse.of(adminUserService.getUser(userId));
    }

    /** 회원 제재. */
    @PutMapping("/{userId}/ban")
    public ResponseEntity<Void> banUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @Valid @RequestBody BanUserRequest request
    ) {
        adminUserService.banUser(requireUserId(principal), userId, request);
        return ResponseEntity.noContent().build();
    }
    /** 제재 해제. */
    @DeleteMapping("/{userId}/ban")
    public ResponseEntity<Void> unbanUser(@PathVariable Long userId) {
        adminUserService.unbanUser(userId);
        return ResponseEntity.noContent().build();
    }
    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
        
}
