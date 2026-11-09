package com.dipsource.dto.admin;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.dipsource.entity.user.UserSanction;

public record AdminSanctionResponse(
    Long id,
    String period,
    String reason,
    String createdAt,
    String admin
) {
    public static AdminSanctionResponse from(UserSanction sanction, String adminNickname) {
        return new AdminSanctionResponse(
            sanction.getId(),
            toPeriod(sanction),
            sanction.getReason(),
            sanction.getCreatedAt() == null
                    ? null
                    : sanction.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            adminNickname
        );
    }

    /** ends_at이 없으면 영구. 있으면 시작~종료 일수. */
    private static String toPeriod(UserSanction sanction) {
        if (sanction.getEndsAt() == null) {
            return "영구";
        }
        long days = ChronoUnit.DAYS.between(sanction.getStartsAt(), sanction.getEndsAt());
        return days + "일";
    }

    
}
