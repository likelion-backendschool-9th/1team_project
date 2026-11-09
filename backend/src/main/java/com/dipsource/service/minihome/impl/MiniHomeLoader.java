package com.dipsource.service.minihome.impl;

import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.minihome.MiniHome;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.minihome.MiniHomeRepository;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 미니홈피 조회·생성. 미니홈피 서비스들이 같은 패키지에서 같이 쓴다. */
@Component
@RequiredArgsConstructor
class MiniHomeLoader {

    private final MiniHomeRepository miniHomeRepository;
    private final UserService userService;

    /** 없으면 기본값으로 만든다. 활동 중인 회원이 아니면 예외. */
    MiniHome loadOrCreate(Long userId) {
        if (!userService.isActiveUser(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다");
        }
        return miniHomeRepository.findByUserId(userId)
                .orElseGet(() -> miniHomeRepository.save(draft(userId)));
    }

    private MiniHome draft(Long userId) {
        return MiniHome.builder()
                .userId(userId)
                .title(nickname(userId))
                .build();
    }

    /** 있으면 반환하고, 없으면 저장하지 않은 기본값을 돌려준다. 조회(GET)에서 행을 만들지 않으려고 쓴다. */
    MiniHome loadOrDraft(Long userId) {
        if (!userService.isActiveUser(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다");
        }
        return miniHomeRepository.findByUserId(userId)
                .orElseGet(() -> draft(userId));
    }

    /** 있으면 반환한다. 없으면 비어 있다. 활동 중인 회원이 아니면 예외. */
    Optional<MiniHome> findByUserId(Long userId) {
        if (!userService.isActiveUser(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다");
        }
        return miniHomeRepository.findByUserId(userId);
    }

    private String nickname(Long userId) {
        Map<Long, UserSummaryResponse> summaries = userService.getSummaries(List.of(userId));
        UserSummaryResponse summary = summaries.get(userId);
        if (summary == null || summary.nickname() == null || summary.nickname().isBlank()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다");
        }
        String nickname = summary.nickname().trim();
        return nickname.length() <= 50 ? nickname : nickname.substring(0, 50);
    }
}
