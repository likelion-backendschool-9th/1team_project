package com.dipsource.service.admin.impl;

import com.dipsource.dto.admin.AdminSanctionResponse;
import com.dipsource.dto.admin.AdminUserDetailResponse;
import com.dipsource.dto.admin.AdminUserResponse;
import com.dipsource.dto.admin.BanUserRequest;
import com.dipsource.entity.post.PostStatus;
import com.dipsource.entity.user.enums.UserRole;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.UserSanction;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.report.ReportRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.repository.user.UserSanctionRepository;
import com.dipsource.service.admin.AdminUserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link AdminUserService} 구현체. */
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String USER_REPORT_TYPE = "USER";

    private final UserRepository userRepository;
    private final UserSanctionRepository userSanctionRepository;
    private final DemoRepository demoRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> listUsers(String q, String status, Pageable pageable) {
        int size = pageable.getPageSize() <= 0 ? 10 : Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable limited = PageRequest.of(pageable.getPageNumber(), size);
        Page<User> page = userRepository.searchForAdmin(parseStatus(status), likePattern(q), limited);
        List<Long> userIds = page.getContent().stream().map(User::getId).toList();
        Map<Long, Integer> demoCounts = userIds.isEmpty()
                ? Map.of()
                : countMap(demoRepository.countActiveByUserIds(userIds));
        Map<Long, Integer> postCounts = userIds.isEmpty()
                ? Map.of()
                : countMap(postRepository.countByUserIdsAndStatus(userIds, PostStatus.ACTIVE));
        Map<Long, Integer> reportCounts = userIds.isEmpty()
                ? Map.of()
                : countMap(reportRepository.countByTargetTypeAndTargetIds(USER_REPORT_TYPE, userIds));
        return PageResponse.of(page, user -> AdminUserResponse.from(
                user,
                providerOf(user),
                demoCounts.getOrDefault(user.getId(), 0),
                postCounts.getOrDefault(user.getId(), 0),
                reportCounts.getOrDefault(user.getId(), 0)));
    }

    @Override 
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다"));
        List<UserSanction> sanctions = userSanctionRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        Map<Long, String> adminNames = adminNicknames(sanctions);
        List<AdminSanctionResponse> items = sanctions.stream()
                .map(sanction -> AdminSanctionResponse.from(
                    sanction,
                    adminNames.getOrDefault(sanction.getAdminId(), "관리자")))
                .toList();
        return new AdminUserDetailResponse(
            toAdminUser(user),
            items);
    }

    @Override
    @Transactional
    public void banUser(Long adminId, Long userId, BanUserRequest request) {
        if (adminId.equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "자기 자신은 제재할 수 없습니다.");
        }
        User user = requireUserForUpdate(userId);
        if (user.isWithdrawn()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "탈퇴한 회원입니다.");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 제재 중인 회원입니다.");
        }
        if (user.getRole() == UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "관리자는 제재할 수 없습니다.");
        }
        LocalDateTime startsAt = LocalDateTime.now();
        UserSanction sanction = UserSanction.builder()
                .user(user)
                .adminId(adminId)
                .reason(request.reason())
                .startsAt(startsAt)
                .endsAt(endsAtOf(request.period(), startsAt))
                .build();
        userSanctionRepository.save(sanction);
        user.suspend();
    }

    @Override
    @Transactional
    public void unbanUser(Long userId) {
        User user = requireUserForUpdate(userId);
        if (user.getStatus() != UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "제재 중인 회원이 아닙니다.");
        }
        userSanctionRepository.findFirstByUser_IdAndReleasedAtIsNullOrderByCreatedAtDesc(userId)
                .ifPresent(UserSanction::release);
        user.unsuspend();
    }

    @Override
    @Transactional
    public void expireOverdueSanctions() {
        List<UserSanction> expired = userSanctionRepository.findExpiredActive(
                LocalDateTime.now(), UserStatus.SUSPENDED);
        for (UserSanction sanction : expired) {
            sanction.release();
            sanction.getUser().unsuspend();
        }
    }

    private User requireUserForUpdate(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다"));
    }

    private AdminUserResponse toAdminUser(User user) {
        return AdminUserResponse.from(
                user,
                providerOf(user),
                (int) demoRepository.countByUserIdAndDeletedAtIsNull(user.getId()),
                (int) postRepository.countByUserIdAndStatus(user.getId(), PostStatus.ACTIVE),
                (int) reportRepository.countByTargetTypeAndTargetId(USER_REPORT_TYPE, user.getId()));
    }

    private static Map<Long, Integer> countMap(List<Object[]> rows) {
        Map<Long, Integer> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return map;
    }

    private static LocalDateTime endsAtOf(String period, LocalDateTime startsAt) {
        return switch (period) {
            case "1일" -> startsAt.plusDays(1);
            case "7일" -> startsAt.plusDays(7);
            case "30일" -> startsAt.plusDays(30);
            case "영구" -> null;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "잘못된 제재 기간입니다.");
        };
    }

    private Map<Long, String> adminNicknames(List<UserSanction> sanctions) {
        List<Long> adminIds = sanctions.stream()
            .map(UserSanction::getAdminId)
            .distinct()
            .toList();
        if (adminIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(adminIds).stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayNickname));
    }

    private static UserStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return switch (status) {
            case "active" -> UserStatus.ACTIVE;
            case "suspended" -> UserStatus.SUSPENDED;
            case "withdrawn" -> UserStatus.DELETED;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "잘못된 회원 상태입니다.");
        };
    }

    private static String likePattern(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        String escaped = q.strip().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    private static String providerOf(User user) {
        if (user.getSocialAccounts() == null || user.getSocialAccounts().isEmpty()) {
            return null;
        }
        return user.getSocialAccounts().getFirst().getProvider().name().toLowerCase(Locale.ROOT);
    }

    
}