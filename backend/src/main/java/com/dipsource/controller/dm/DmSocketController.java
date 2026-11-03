package com.dipsource.controller.dm;

import com.dipsource.dto.dm.DmMessageResponse;
import com.dipsource.dto.dm.DmSendRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.service.dm.DmService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * DM 실시간 전송(STOMP).
 * 보내기: /app/dm.send · 받기: /user/queue/dm · 오류: /user/queue/errors
 */
@Controller
@RequiredArgsConstructor
public class DmSocketController {

    /** 회원별 DM 수신 주소. 클라이언트는 /user/queue/dm 을 구독한다. */
    private static final String DM_QUEUE = "/queue/dm";

    private final DmService dmService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/dm.send")
    public void send(@Valid @Payload DmSendRequest request, Principal principal) {
        Long senderId = currentUserId(principal);
        DmMessageResponse message = dmService.send(senderId, request);

        // 받는 사람과 보낸 사람(다른 탭·기기 포함) 모두에게 보낸다. 회원 이름은 userId 문자열이다.
        messagingTemplate.convertAndSendToUser(String.valueOf(message.receiverId()), DM_QUEUE, message);
        messagingTemplate.convertAndSendToUser(String.valueOf(senderId), DM_QUEUE, message);
    }

    /** 처리 중 오류는 보낸 사람에게만 알린다. */
    @MessageExceptionHandler
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public Map<String, String> handleError(Exception e) {
        String message;
        if (e instanceof BusinessException) {
            message = e.getMessage();
        } else if (e instanceof MethodArgumentNotValidException) {
            message = "메시지는 1~1000자로 입력해 주세요.";
        } else {
            message = "메시지를 보내지 못했어요.";
        }
        return Map.of("message", message);
    }

    private Long currentUserId(Principal principal) {
        // 연결할 때 JwtAuthenticationFilter가 넣은 로그인 정보가 그대로 들어온다.
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof UserPrincipal user) {
            return user.getUserId();
        }
        throw new IllegalStateException("로그인 정보가 없어요.");
    }
}
