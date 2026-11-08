package com.dipsource.service.admin.impl;

import com.dipsource.dto.admin.AdminDashboardResponse;
import com.dipsource.entity.report.ReportStatus;
import com.dipsource.repository.demo.DemoRepository;
import com.dipsource.repository.report.ReportRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.admin.AdminDashboardService;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link AdminDashboardService} 구현체. */
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final DemoRepository demoRepository;
    private final ReportRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse get() {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = monday.plusWeeks(1).atStartOfDay();
        LocalDateTime todayStart = today.atStartOfDay();

        int[] byDay = new int[7];
        for (Object[] row : userRepository.countSignupsByDay(weekStart, weekEnd)) {
            LocalDate day = toLocalDate(row[0]);
            int index = (int) ChronoUnit.DAYS.between(monday, day);
            if (index >= 0 && index < 7) {
                byDay[index] = ((Number) row[1]).intValue();
            }
        }
        List<Integer> signups = new ArrayList<>(7);
        for (int count : byDay) {
            signups.add(count);
        }

        return new AdminDashboardResponse(
                userRepository.count(),
                userRepository.countByCreatedAtGreaterThanEqual(todayStart),
                demoRepository.countByDeletedAtIsNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        weekStart, weekEnd),
                reportRepository.countByStatus(ReportStatus.PENDING),
                0,
                signups);
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }
}
