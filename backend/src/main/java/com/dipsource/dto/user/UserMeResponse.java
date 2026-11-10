package com.dipsource.dto.user;

import com.dipsource.entity.user.User;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/** 내 정보 조회 응답. */
public record UserMeResponse(
        Long id,
        String nickname,
        String email,
        String bio,
        String profileImage,
        String githubUsername,
        List<String> techStacks,
        boolean onboardingIncomplete,
        List<Object> badges,
        boolean plus,
        String company,
        int followerCount,
        int followingCount,
        int chipBalance,
        // USER / ADMIN / MASTER. 프론트가 관리자 화면 접근을 막는 데 쓴다 (실제 권한 검사는 SecurityConfig의 /admin/**)
        String role,
        // 칩으로 늘린 데모 인벤토리 칸 수. 데모 올리기 화면에서 남은 칸을 계산할 때 쓴다
        int extraInventorySlots,
        // 딥소스 플러스 만료 시각. 구독 화면의 다음 결제일로 쓴다. 없으면 null
        LocalDateTime plusUntil,
        // 자동 갱신. 해지 후에는 false이고, 만료 전이어도 다음 결제는 없다
        boolean plusAutoRenew
) {
    /**
     * @param followerCount  나를 팔로우하는 활동 회원 수
     * @param followingCount 내가 팔로우하는 활동 회원 수
     */
    public static UserMeResponse from(User user, long followerCount, long followingCount) {
        return new UserMeResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getBio(),
                user.getProfileImage(),
                user.getGithubUsername(),
                user.getTechStacks() == null ? List.of() : List.copyOf(user.getTechStacks()),
                user.isOnboardingIncomplete(),
                List.of(),   // badges — 아직 미구현
                user.isPlus(LocalDateTime.now(ZoneId.of("Asia/Seoul"))),
                null,        // company
                Math.toIntExact(followerCount),
                Math.toIntExact(followingCount),
                user.getChipBalance(),
                user.getRole() == null ? "USER" : user.getRole().name(),
                user.getExtraInventorySlots(),
                user.getPlusUntil(),
                user.isPlusAutoRenew()
        );
    }
}
