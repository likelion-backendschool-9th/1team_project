package com.dipsource.service.user;

import com.dipsource.dto.user.CompleteProfileRequest;
import com.dipsource.dto.user.NicknameAvailableResponse;
import com.dipsource.dto.user.UserMeResponse;
import com.dipsource.dto.user.UserProfileResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** 회원 조회·온보딩 프로필 저장. */
public interface UserService {

    /** 내 정보 조회 */
    UserMeResponse getMe(Long userId);

    UserProfileResponse getProfile(Long userId, Long viewerId);

    /** 닉네임 사용 가능 여부 (본인 닉네임은 사용 가능으로 본다) */
    NicknameAvailableResponse isNicknameAvailable(Long userId, String nickname);

    /** 온보딩에서 모은 프로필·태그를 저장한다 */
    UserMeResponse completeProfile(Long userId, CompleteProfileRequest request);

    /** 탈퇴·정지되지 않은 활동 중인 회원인지 확인한다. 다른 기능(DM 등)에서 쓴다. */
    boolean isActiveUser(Long userId);

    /** 여러 회원의 요약 정보. 없는 회원은 결과에서 빠진다. */
    Map<Long, UserSummaryResponse> getSummaries(Collection<Long> userIds);

    /** 이 중 최근 활동이 있어 "접속 중"으로 볼 회원 ID */
    Set<Long> getOnlineUserIds(Collection<Long> userIds);

    /** 마지막 활동 시각을 지금으로 갱신한다. */
    void markActive(Long userId);
}
