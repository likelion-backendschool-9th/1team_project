package com.dipsource.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP 설정. DM·알림 실시간 전송에 쓴다.
 * 인증은 연결(handshake) 요청의 access_token 쿠키로 한다. 연결 요청도 일반 HTTP 요청이라
 * JwtAuthenticationFilter를 거치고, 로그인한 회원만 연결된다(SecurityConfig의 anyRequest().authenticated()).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 기본 연결 주소. 브라우저 WebSocket으로 바로 붙는다. 예: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendBaseUrl);

        // WebSocket이 막힌 환경을 위한 SockJS 대체 연결 주소. 예: http://localhost:8080/ws-sockjs
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOrigins(frontendBaseUrl)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트 → 서버: /app/... 으로 보내면 @MessageMapping 메서드가 받는다.
        registry.setApplicationDestinationPrefixes("/app");
        // 서버 → 클라이언트: /topic(여러 명), /queue(한 명)를 메모리 브로커가 전달한다.
        // 10초마다 연결 확인 신호(heartbeat)를 주고받는다. 대화가 없어도 Nginx가 연결을 끊지 않게 한다(기본 60초).
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(heartbeatScheduler());
        // 특정 회원에게 보낼 때 쓰는 접두사. 클라이언트는 /user/queue/dm 을 구독한다.
        registry.setUserDestinationPrefix("/user");
    }

    /** heartbeat 전송용 스케줄러 */
    @Bean
    public TaskScheduler heartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }
}
