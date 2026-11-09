package com.dipsource.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 권한 변경 요청. 마스터만 보낼 수 있다.
 *
 * @param role ADMIN(관리자로 지정) 또는 USER(일반 회원으로 강등). MASTER는 줄 수 없다
 */
public record ChangeRoleRequest(
        @NotNull @Pattern(regexp = "ADMIN|USER", message = "권한은 ADMIN 또는 USER만 줄 수 있어요.") String role
) {
}
