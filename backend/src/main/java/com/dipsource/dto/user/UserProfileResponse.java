package com.dipsource.dto.user;

import com.dipsource.entity.user.User;
import java.util.List;

public record UserProfileResponse(
    Long id,
    String nickname,
    String profileImage,
    List<String> techStacks,
    List<BadgeResponse> badges,
    String company,
    boolean plus,
    boolean following,
    int extraInventorySlots
) {
    public record BadgeResponse(Long id, String icon, String label, String tone) {}

    public static UserProfileResponse of(User user, boolean following) {
        UserSummaryResponse summary = UserSummaryResponse.from(user);
        return new UserProfileResponse(
            summary.id(), summary.nickname(), summary.profileImage(), summary.techStacks(),
            List.of(),
            null,
            false,
            following,
            user.getExtraInventorySlots()
        );
    }
}
