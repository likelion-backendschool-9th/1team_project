package com.dipsource.controller.dm;

import com.dipsource.dto.dm.DmMessageItemResponse;
import com.dipsource.dto.dm.DmRoomResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.dm.DmService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DM 대화방 목록·대화 내역 API. 메시지 보내기는 WebSocket(STOMP)으로 한다({@link DmSocketController}).
 */
@RestController
@RequestMapping("/dm")
@RequiredArgsConstructor
public class DmController {

    private final DmService dmService;

    /** 내 대화방 목록. 최근 메시지 순 */
    @GetMapping("/rooms")
    public ApiResponse<List<DmRoomResponse>> getRooms(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.of(dmService.getRooms(requireLogin(principal)));
    }

    /** 상대와의 대화 내역. 0페이지가 가장 최근이고, 페이지 안은 오래된 → 최신 순이다. */
    @GetMapping("/{userId}")
    public ApiResponse<PageResponse<DmMessageItemResponse>> getMessages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return ApiResponse.of(dmService.getMessages(requireLogin(principal), userId, pageable));
    }

    /** GET은 비로그인도 통과하므로(SecurityConfig) DM은 여기서 로그인을 확인한다. */
    private Long requireLogin(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return principal.getUserId();
    }
}
