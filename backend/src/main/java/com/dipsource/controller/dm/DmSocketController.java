package com.dipsource.controller.dm;

import com.dipsource.dto.dm.DmMessageResponse;
import com.dipsource.dto.dm.DmReadRequest;
import com.dipsource.dto.dm.DmSendRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.websocket.UserMessageSender;
import com.dipsource.service.dm.DmService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * DM 실시간 전송(STOMP).
 * 보내기: /app/dm.send · 받기: /user/queue/dm · 오류: /user/queue/errors
 * 읽음: /app/dm.read 로 알리면 상대가 /user/queue/dm-read 로 받는다.
 */
@Controller
@RequiredArgsConstructor
public class DmSocketController {

    private final DmService dmService;
    private final UserMessageSender userMessageSender;

    @MessageMapping("/dm.send")
    public void send(@Valid @Payload DmSendRequest request, Principal principal) {
        Long senderId = currentUserId(principal);
        DmMessageResponse message = dmService.send(senderId, request);

        // 받는 사람과 보낸 사람(다른 탭·기기 포함) 모두에게 보낸다. 회원 이름은 userId 문자열이다.
        userMessageSender.send(message.receiverId(), UserMessageSender.DM, message);
        userMessageSender.send(senderId, UserMessageSender.DM, message);
    }

    /** 대화를 보고 있는 동안 받은 메시지를 읽음 처리한다. */
    @MessageMapping("/dm.read")
    public void read(@Valid @Payload DmReadRequest request, Principal principal) {
        dmService.markRead(currentUserId(principal), request.partnerId());
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
