package com.dipsource.dto.admin;

import java.util.List;

public record AdminUserDetailResponse(
    AdminUserResponse user,
    List<AdminSanctionResponse> sanctions
) {}
