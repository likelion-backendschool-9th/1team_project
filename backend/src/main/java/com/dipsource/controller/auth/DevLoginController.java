package com.dipsource.controller.auth;

import com.dipsource.dto.auth.DevLoginRequest;
import com.dipsource.dto.auth.DevLoginResponse;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.UserStatus;
import com.dipsource.global.auth.JwtProvider;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TEMP-DEV-LOGIN: 로컬 시드 계정용 가짜 로그인.
 * app.dev-login-enabled=true 일 때만 빈으로 등록된다. 배포에서는 끄고, 이 클래스·프론트 /login/dev 를 삭제한다.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.dev-login-enabled", havingValue = "true")
public class DevLoginController {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @PostMapping("/dev-login")
    public ApiResponse<DevLoginResponse> devLogin(@Valid @RequestBody DevLoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "시드 계정이 없어요. ./up.sh 로 시드를 넣었는지 확인하세요."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "활성 계정이 아니에요.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        return ApiResponse.of(new DevLoginResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getNickname(),
                user.getEmail()
        ));
    }
}
