package com.onandhome.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration

// STOMP(WebSocket 기반 메시징) 기능을 활성화하는 설정.
// 실시간 알림, 관리자 브로드캐스트, 개인 알림 등에 필요한 핵심 기능.
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // 서버 → 클라이언트 방향의 메시지 경로(prefix) 설정
        //
        // "/topic" : 전체 사용자에게 보내는 broadcast 메시지
        //            예: 관리자 → 전체 사용자 공지, 전체 알림 등
        //
        // "/queue" : 특정 사용자에게 보내는 개인 메시지(1:1)
        //            예: "내 QnA 답변", "내 리뷰 답글", "내 주문 완료"
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트 → 서버로 메시지를 보낼 때 사용하는 prefix.
        // 예: 프론트에서 "/app/hello"로 보내면 서버의 @MessageMapping("/hello")가 실행됨.
        config.setApplicationDestinationPrefixes("/app");

        // 서버가 특정 사용자에게 알림 보낼 때 사용하는 prefix.
        // 실제 전송 경로는 "/user/{userId}/queue/…"
        // convertAndSendToUser(userId, "/queue/notifications", data);
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // WebSocket 연결 엔드포인트.
        // 클라이언트는 "/ws"로 접속해야 WebSocket 또는 SockJS가 연결됨.
        registry.addEndpoint("/ws")

                // React 개발 환경(CORS 허용)
                .setAllowedOriginPatterns("http://localhost:3000")

                // WebSocket을 지원하지 않는 브라우저에서도 통신 가능하도록 SockJS 사용
                .withSockJS();
    }
}

/*
요약
1. "/topic" → 여러 사용자에게 동시에 보내는 broadcast 알림용 경로.
2. "/queue" + "/user" → 특정 사용자에게만 보내는 개인 알림 경로.
3. "/app" → 프론트가 서버로 메시지를 보낼 때 사용하는 prefix.
4. "/ws" → WebSocket/SockJS 연결의 실제 엔드포인트.
5. 실시간 알림, 관리자 알림, 1:1 사용자 알림 구조를 모두 총괄하는 핵심 설정 파일.
*/
