package com.dipsource.dto.admin;

import java.util.List;

/** GET /admin/dashboard. 매출은 결제 API가 생길 때까지 0. */
public record AdminDashboardResponse(
        long userCount,
        long newUsersToday,
        long demoUploadsWeek,
        long pendingReports,
        long revenueMonth,
        List<Integer> signups) {
}
