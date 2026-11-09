package com.dipsource.controller.search;

import com.dipsource.dto.search.SearchHiddenItemResponse;
import com.dipsource.dto.search.SearchTargetRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.search.SearchPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 사용자별 검색 설정. 모두 로그인이 필요하고, 사용자는 JWT에서만 알아낸다. */
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchPreferenceController {

    private static final int MAX_PAGE_SIZE = 50;

    private final SearchPreferenceService preferenceService;

    /** 검색 결과 숨김. 이미 숨겼어도 성공이다. */
    @PostMapping("/hidden")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hide(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody SearchTargetRequest request) {
        preferenceService.hide(userId(principal), request.targetType(), request.targetId());
    }

    /** 숨김 해제. 숨긴 적이 없어도 성공이다. */
    @DeleteMapping("/hidden/{targetType}/{targetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unhide(@AuthenticationPrincipal UserPrincipal principal,
                       @PathVariable String targetType, @PathVariable Long targetId) {
        preferenceService.unhide(userId(principal), targetType, targetId);
    }

    /** 내가 숨긴 검색 결과 */
    @GetMapping("/hidden")
    public ApiResponse<PageResponse<SearchHiddenItemResponse>> hidden(@AuthenticationPrincipal UserPrincipal principal,
                                                                      @PageableDefault(size = 20) Pageable pageable) {
        Pageable limited = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE));
        return ApiResponse.of(preferenceService.hiddenList(userId(principal), limited));
    }

    /** 검색 결과에서 문서로 들어갔을 때 호출한다. 조회수와는 별개다. */
    @PostMapping("/visits")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void visit(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody SearchTargetRequest request) {
        preferenceService.recordVisit(userId(principal), request.targetType(), request.targetId(), request.query());
    }

    private static Long userId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return principal.getUserId();
    }
}
