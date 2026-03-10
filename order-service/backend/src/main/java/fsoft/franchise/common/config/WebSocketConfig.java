package fsoft.franchise.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket + STOMP cho real-time order tracking.
 *
 * <h3>Kiến trúc:</h3>
 * 
 * <pre>
 *   Client (browser)
 *     │
 *     │  1. Connect tới ws://host/ws (STOMP handshake)
 *     │  2. SUBSCRIBE /topic/orders/{orderId}
 *     │
 *     ▼
 *   STOMP Endpoint "/ws"
 *     │
 *     ▼
 *   In-memory Message Broker (prefix: /topic)
 *     │
 *     │  Server gửi message tới /topic/orders/{orderId}
 *     │  → Tất cả client đang subscribe sẽ nhận được
 *     ▼
 *   Client nhận OrderTrackingDTO (JSON)
 * </pre>
 *
 * <h3>Tại sao dùng STOMP?</h3>
 * - Raw WebSocket chỉ là "ống dẫn byte" — bạn phải tự xử lý routing,
 * serialization
 * - STOMP thêm lớp abstraction: subscribe/publish topics, tự serialize JSON
 * - Spring hỗ trợ STOMP rất tốt qua SimpMessagingTemplate
 */
@Configuration
@EnableWebSocketMessageBroker // Bật WebSocket message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Cấu hình message broker (bộ điều phối tin nhắn).
     *
     * - enableSimpleBroker("/topic"): Dùng broker in-memory, messages gửi tới
     * destination bắt đầu bằng "/topic" sẽ được route tới subscribers.
     * VD: server gửi tới "/topic/orders/abc-123" → tất cả client subscribe
     * "/topic/orders/abc-123" đều nhận được.
     *
     * - setApplicationDestinationPrefixes("/app"): Messages từ client gửi lên
     * có prefix "/app" sẽ được route tới @MessageMapping methods.
     * VD: client gửi tới "/app/hello" → tìm @MessageMapping("/hello")
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Broker in-memory, xử lý messages có destination prefix "/topic"
        config.enableSimpleBroker("/topic");
        // Prefix cho messages từ client gửi lên server
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Đăng ký STOMP endpoint — đây là URL mà client connect WebSocket tới.
     *
     * - addEndpoint("/ws"): Client connect tới ws://host:port/ws
     * - setAllowedOriginPatterns("*"): Cho phép mọi origin (dev). Production nên
     * giới hạn.
     * - withSockJS(): Fallback cho browser không hỗ trợ WebSocket (dùng
     * long-polling)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CORS cho WebSocket
                .withSockJS(); // Fallback cho browser cũ
    }
}
