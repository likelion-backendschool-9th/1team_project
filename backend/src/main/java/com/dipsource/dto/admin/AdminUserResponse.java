package com.dipsource.dto.admin;

import java.time.format.DateTimeFormatter;

import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;

public record AdminUserResponse(
    Long id,
    String nickname,
    String email,
    String provider,
    String status,
    String joinedAt,
    int demoCount,
    int postCount,
    int reportCount,
    int chipBalance
) {
    public static AdminUserResponse from(
        User user,
        String provider,
        int demoCount,
        int postCount,
        int reportCount
    ) {
        return new AdminUserResponse(
            user.getId(),
            user.getNickname(),
            user.getEmail(),
            provider,
            toStatus(user.getStatus()),
            user.getCreatedAt() == null ? null : user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            demoCount,
            postCount,
            reportCount,
            user.getChipBalance()
        );
    }

    private static String toStatus(UserStatus status) {
        if (status == UserStatus.SUSPENDED) {
            return "suspended";
        }
        if (status == UserStatus.DELETED) {
            return "withdrawn";
        }
        return "active";
    }
} 
