package com.studyback.smartfleet.websocket;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.studyback.smartfleet.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 监控 WebSocket 处理器
 * <p>处理 WebSocket 连接的建立、关闭和消息接收</p>
 * <p>支持客户端发送订阅/取消订阅命令</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        webSocketService.handleConnection(session);
        log.info("监控 WebSocket 连接建立: sessionId={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.debug("收到 WebSocket 消息: sessionId={}, payload={}", session.getId(), payload);

        try {
            JsonNode node = objectMapper.readTree(payload);
            String type = node.has("type") ? node.get("type").asText() : "";

            switch (type) {
                case "subscribe":
                    String topic = node.get("topic").asText();
                    webSocketService.subscribe(session.getId(), topic);
                    // 发送订阅确认
                    webSocketService.sendMessage(session,
                            objectMapper.writeValueAsString(new SubscribeResponse("subscribed", topic)));
                    break;
                case "unsubscribe":
                    String unsubTopic = node.get("topic").asText();
                    webSocketService.unsubscribe(session.getId(), unsubTopic);
                    // 发送取消订阅确认
                    webSocketService.sendMessage(session,
                            objectMapper.writeValueAsString(new SubscribeResponse("unsubscribed", unsubTopic)));
                    break;
                default:
                    log.warn("未知的 WebSocket 消息类型: type={}", type);
                    break;
            }
        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败: sessionId={}, payload={}", session.getId(), payload, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        webSocketService.handleDisconnection(session);
        log.info("监控 WebSocket 连接关闭: sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 传输错误: sessionId={}", session.getId(), exception);
        webSocketService.handleDisconnection(session);
    }

    /**
     * 订阅/取消订阅响应
     */
    private record SubscribeResponse(String type, String topic) {
    }
}
