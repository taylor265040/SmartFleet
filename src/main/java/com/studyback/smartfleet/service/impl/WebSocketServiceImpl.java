package com.studyback.smartfleet.service.impl;

import com.studyback.smartfleet.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket 服务实现
 * <p>管理 WebSocket 连接、消息发送、主题订阅</p>
 */
@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {

    /**
     * 活跃会话映射：sessionId -> WebSocketSession
     */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 主题订阅映射：topic -> Set<sessionId>
     */
    private final Map<String, Set<String>> topicSubscribers = new ConcurrentHashMap<>();

    /**
     * 会话订阅映射：sessionId -> Set<topic>（用于断开时清理）
     */
    private final Map<String, Set<String>> sessionTopics = new ConcurrentHashMap<>();

    @Override
    public void handleConnection(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        sessionTopics.put(sessionId, new CopyOnWriteArraySet<>());
        log.info("WebSocket 连接建立: sessionId={}, 当前活跃连接数={}", sessionId, sessions.size());
    }

    @Override
    public void handleDisconnection(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.remove(sessionId);

        // 清理该会话的所有主题订阅
        Set<String> subscribedTopics = sessionTopics.remove(sessionId);
        if (subscribedTopics != null) {
            for (String topic : subscribedTopics) {
                Set<String> subscribers = topicSubscribers.get(topic);
                if (subscribers != null) {
                    subscribers.remove(sessionId);
                    if (subscribers.isEmpty()) {
                        topicSubscribers.remove(topic);
                    }
                }
            }
        }

        log.info("WebSocket 连接断开: sessionId={}, 当前活跃连接数={}", sessionId, sessions.size());
    }

    @Override
    public void sendMessage(WebSocketSession session, String message) {
        if (session == null || !session.isOpen()) {
            log.debug("WebSocket 会话已关闭，跳过消息发送");
            return;
        }
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            log.error("WebSocket 消息发送失败: sessionId={}", session.getId(), e);
            // 发送失败时移除无效会话
            handleDisconnection(session);
        }
    }

    @Override
    public void broadcast(String message) {
        log.debug("WebSocket 广播消息: 活跃连接数={}", sessions.size());
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            sendMessage(entry.getValue(), message);
        }
    }

    @Override
    public void sendToSubscribers(String topic, String message) {
        Set<String> subscribers = topicSubscribers.get(topic);
        if (subscribers == null || subscribers.isEmpty()) {
            log.debug("WebSocket 主题无订阅者: topic={}", topic);
            return;
        }
        log.debug("WebSocket 推送主题消息: topic={}, 订阅者数={}", topic, subscribers.size());
        for (String sessionId : subscribers) {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null) {
                sendMessage(session, message);
            }
        }
    }

    @Override
    public void subscribe(String sessionId, String topic) {
        topicSubscribers.computeIfAbsent(topic, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        sessionTopics.computeIfAbsent(sessionId, k -> new CopyOnWriteArraySet<>()).add(topic);
        log.info("WebSocket 订阅主题: sessionId={}, topic={}", sessionId, topic);
    }

    @Override
    public void unsubscribe(String sessionId, String topic) {
        Set<String> subscribers = topicSubscribers.get(topic);
        if (subscribers != null) {
            subscribers.remove(sessionId);
            if (subscribers.isEmpty()) {
                topicSubscribers.remove(topic);
            }
        }
        Set<String> topics = sessionTopics.get(sessionId);
        if (topics != null) {
            topics.remove(topic);
        }
        log.info("WebSocket 取消订阅: sessionId={}, topic={}", sessionId, topic);
    }

    @Override
    public boolean isSessionActive(String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        return session != null && session.isOpen();
    }

    @Override
    public int getActiveSessionCount() {
        return sessions.size();
    }
}
