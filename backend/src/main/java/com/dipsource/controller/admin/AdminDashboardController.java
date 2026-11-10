package com.dipsource.controller.admin;

import com.dipsource.dto.admin.AdminDashboardResponse;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ApiResponse<AdminDashboardResponse> get() {
        return ApiResponse.of(adminDashboardService.get());
    }
}
