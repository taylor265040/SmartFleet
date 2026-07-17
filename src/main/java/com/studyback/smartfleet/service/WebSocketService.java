package com.studyback.smartfleet.service;

import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 服务接口
 * <p>提供 WebSocket 连接管理、消息发送、主题订阅等功能</p>
 */
public interface WebSocketService {

    /**
     * 处理连接建立
     *
     * @param session WebSocket 会话
     */
    void handleConnection(WebSocketSession session);

    /**
     * 处理连接断开
     *
     * @param session WebSocket 会话
     */
    void handleDisconnection(WebSocketSession session);

    /**
     * 发送消息给指定会话
     *
     * @param session WebSocket 会话
     * @param message 消息内容（JSON 字符串）
     */
    void sendMessage(WebSocketSession session, String message);

    /**
     * 广播消息给所有已连接的会话
     *
     * @param message 消息内容（JSON 字符串）
     */
    void broadcast(String message);

    /**
     * 发送消息给订阅了指定主题的所有会话
     *
     * @param topic   主题名称
     * @param message 消息内容（JSON 字符串）
     */
    void sendToSubscribers(String topic, String message);

    /**
     * 订阅主题
     *
     * @param sessionId 会话ID
     * @param topic     主题名称
     */
    void subscribe(String sessionId, String topic);

    /**
     * 取消订阅主题
     *
     * @param sessionId 会话ID
     * @param topic     主题名称
     */
    void unsubscribe(String sessionId, String topic);

    /**
     * 判断会话是否活跃
     *
     * @param sessionId 会话ID
     * @return 是否活跃
     */
    boolean isSessionActive(String sessionId);

    /**
     * 获取活跃连接数
     *
     * @return 活跃连接数
     */
    int getActiveSessionCount();
}
