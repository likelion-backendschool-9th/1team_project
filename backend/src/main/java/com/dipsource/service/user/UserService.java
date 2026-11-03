package com.dipsource.service.user;

import com.dipsource.dto.user.CompleteProfileRequest;
import com.dipsource.dto.user.NicknameAvailableResponse;
import com.dipsource.dto.user.UserMeResponse;

/** 회원 조회·온보딩 프로필 저장. */
public interface UserService {

    /** 내 정보 조회 */
    UserMeResponse getMe(Long userId);

    /** 닉네임 사용 가능 여부 (본인 닉네임은 사용 가능으로 본다) */
    NicknameAvailableResponse isNicknameAvailable(Long userId, String nickname);

    /** 온보딩에서 모은 프로필·태그를 저장한다 */
    UserMeResponse completeProfile(Long userId, CompleteProfileRequest request);
}
