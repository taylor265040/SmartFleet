package com.studyback.smartfleet.service;

/**
 * RocketMQ 消息生产者服务接口
 * <p>提供同步、延迟、异步消息发送能力</p>
 */
public interface RocketMQProducerService {

    /**
     * 同步发送消息
     *
     * @param topic   主题
     * @param message 消息体（JSON 字符串）
     */
    void send(String topic, String message);

    /**
     * 延迟发送消息
     *
     * @param topic      主题
     * @param message    消息体（JSON 字符串）
     * @param delayLevel 延迟级别（1-18，对应不同延迟时间）
     */
    void sendWithDelay(String topic, String message, int delayLevel);

    /**
     * 异步发送消息
     *
     * @param topic   主题
     * @param message 消息体（JSON 字符串）
     */
    void sendAsync(String topic, String message);
}
