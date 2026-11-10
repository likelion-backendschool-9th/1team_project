package com.dipsource.controller.admin;

import com.dipsource.dto.admin.ChangeRoleRequest;
import com.dipsource.dto.admin.RoleUserResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.service.admin.MasterRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 권한 관리 — 최고 관리자(MASTER)만 (SecurityConfig /admin/roles/** hasRole MASTER).
 * GET /admin/roles/users?q=&role=ADMIN|USER&page=&size=
 * PUT /admin/roles/users/{userId} { "role": "ADMIN" | "USER" }
 */
@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class MasterRoleController {

    private final MasterRoleService masterRoleService;

    @GetMapping("/users")
    public ApiResponse<PageResponse<RoleUserResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String safeRole = role == null || role.isBlank() ? null : role.strip();
        return ApiResponse.of(masterRoleService.list(q, safeRole, page, size));
    }

    @PutMapping("/users/{userId}")
    public ApiResponse<RoleUserResponse> changeRole(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @Valid @RequestBody ChangeRoleRequest request
    ) {
        return ApiResponse.of(masterRoleService.changeRole(principal.getUserId(), userId, request.role()));
    }
}
