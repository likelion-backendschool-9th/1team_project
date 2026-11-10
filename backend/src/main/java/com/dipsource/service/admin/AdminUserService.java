package com.dipsource.service.admin;

import org.springframework.data.domain.Pageable;

import com.dipsource.dto.admin.AdminUserDetailResponse;
import com.dipsource.dto.admin.AdminUserResponse;
import com.dipsource.dto.admin.BanUserRequest;
import com.dipsource.global.common.PageResponse;

public interface AdminUserService {

    /** 회원 목록. q는 닉네임·이메일, status는 active/suspended/withdrawn. */
    PageResponse<AdminUserResponse> listUsers(String q, String status, Pageable pageable);

    AdminUserDetailResponse getUser(Long userId);

      /** 회원 제재. users.status를 SUSPENDED로 바꾸고 user_sanctions에 이력을 남긴다. */
    void banUser(Long adminId, Long userId, BanUserRequest request);

    /** 제재 해제. 적용 중인 이력에 released_at을 넣고 status를 ACTIVE로 되돌린다. */
    void unbanUser(Long userId);

    /** 기간이 지난 제재를 해제하고 회원 상태를 ACTIVE로 되돌린다. */
    void expireOverdueSanctions();
} 
