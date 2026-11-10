package com.dipsource.global.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 특정 회원에게 WebSocket(STOMP)으로 보낸다. 클라이언트는 /user{destination} 을 구독한다.
 * 연결돼 있지 않은 회원에게 보내면 아무 일도 일어나지 않는다.
 */
@Component
@RequiredArgsConstructor
public class UserMessageSender {

    /** DM 메시지 */
    public static final String DM = "/queue/dm";
    /** 상대가 내 DM을 읽었다는 표시 */
    public static final String DM_READ = "/queue/dm-read";
    /** 새 알림 */
    public static final String NOTIFICATIONS = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 트랜잭션 안에서 부르면 커밋된 뒤에 보낸다. 롤백되면 보내지 않는다.
     * 회원 이름은 userId 문자열이다(JwtAuthenticationFilter가 넣은 로그인 정보).
     */
    public void send(Long userId, String destination, Object payload) {
        Runnable task = () -> messagingTemplate.convertAndSendToUser(String.valueOf(userId), destination, payload);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }
}
