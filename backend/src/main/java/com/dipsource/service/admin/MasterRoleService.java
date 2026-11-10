package com.dipsource.service.admin;

import com.dipsource.dto.admin.RoleUserResponse;
import com.dipsource.entity.admin.AdminActionLog;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserRole;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.admin.AdminActionLogRepository;
import com.dipsource.repository.user.UserRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 최고 관리자(MASTER)의 권한 부여. 회원을 관리자(ADMIN)로 지정하거나 일반 회원(USER)으로 내린다.
 *
 * <p>바뀐 권한은 JWT에 들어 있어서, 대상 회원의 Access Token이 다시 발급될 때(토큰 갱신·재로그인) 반영된다.
 */
@Service
@RequiredArgsConstructor
public class MasterRoleService {

    private static final int MAX_PAGE_SIZE = 50;

    private final UserRepository userRepository;
    private final AdminActionLogRepository adminActionLogRepository;

    /**
     * @param role null이면 전체, ADMIN이면 관리자(마스터 포함), USER면 일반 회원만
     */
    @Transactional(readOnly = true)
    public PageResponse<RoleUserResponse> list(@Nullable String q, @Nullable String role, int page, int size) {
        List<UserRole> roles = switch (role == null ? "" : role) {
            case "" -> List.of(UserRole.values());
            case "ADMIN" -> List.of(UserRole.MASTER, UserRole.ADMIN);
            case "USER" -> List.of(UserRole.USER);
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "권한 필터는 ADMIN 또는 USER만 쓸 수 있어요.");
        };
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageResponse.of(
                userRepository.searchForRoles(roles, likePattern(q), PageRequest.of(Math.max(page, 0), safeSize)),
                RoleUserResponse::from);
    }

    @Transactional
    public RoleUserResponse changeRole(Long masterId, Long targetUserId, String role) {
        UserRole next = UserRole.valueOf(role);
        if (next == UserRole.MASTER) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "마스터 권한은 줄 수 없어요.");
        }
        if (masterId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "자기 자신의 권한은 바꿀 수 없어요.");
        }
        User master = userRepository.findById(masterId)
                .filter(user -> user.getRole() == UserRole.MASTER)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "마스터만 권한을 바꿀 수 있어요."));
        User target = userRepository.findByIdForUpdate(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요."));
        if (target.isWithdrawn()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "탈퇴한 회원이에요.");
        }
        if (target.getRole() == UserRole.MASTER) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "마스터의 권한은 바꿀 수 없어요.");
        }
        if (target.getRole() == next) {
            return RoleUserResponse.from(target);
        }

        UserRole before = target.getRole();
        target.changeRole(next);
        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(master)
                .actionType(next == UserRole.ADMIN ? "GRANT_ADMIN" : "REVOKE_ADMIN")
                .targetType("USER")
                .targetId(target.getId())
                .reason(before.name() + " -> " + next.name())
                .build());
        return RoleUserResponse.from(target);
    }

    private static String likePattern(@Nullable String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        String escaped = q.strip().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
