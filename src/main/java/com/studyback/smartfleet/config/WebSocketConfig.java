package com.studyback.smartfleet.config;

import com.studyback.smartfleet.websocket.MonitoringWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.List;

/**
 * WebSocket 配置
 * <p>注册 WebSocket 处理器，配置端点和允许的来源</p>
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MonitoringWebSocketHandler monitoringWebSocketHandler;

    @Value("${websocket.allowed-origins:http://localhost:8080,http://localhost:3000}")
    private List<String> allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(monitoringWebSocketHandler, "/ws/monitoring")
                .setAllowedOrigins(allowedOrigins.toArray(new String[0]));
    }
}
