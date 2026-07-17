package com.studyback.smartfleet.service.impl;

import com.studyback.smartfleet.service.RocketMQProducerService;
import com.studyback.smartfleet.util.RocketMQUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * RocketMQ 消息生产者服务实现
 * <p>通过 RocketMQUtil 工具类发送消息</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RocketMQProducerServiceImpl implements RocketMQProducerService {

    private final RocketMQUtil rocketMQUtil;

    /**
     * 同步发送消息
     */
    @Override
    public void send(String topic, String message) {
        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException("消息主题不能为空");
        }
        if (message == null) {
            throw new IllegalArgumentException("消息体不能为空");
        }
        log.info("同步发送消息: topic={}", topic);
        rocketMQUtil.syncSend(topic, message);
    }

    /**
     * 延迟发送消息
     */
    @Override
    public void sendWithDelay(String topic, String message, int delayLevel) {
        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException("消息主题不能为空");
        }
        if (message == null) {
            throw new IllegalArgumentException("消息体不能为空");
        }
        if (delayLevel < 1 || delayLevel > 18) {
            throw new IllegalArgumentException("延迟级别必须在 1-18 之间");
        }
        log.info("延迟发送消息: topic={}, delayLevel={}", topic, delayLevel);
        rocketMQUtil.syncSendDelay(topic, message, delayLevel);
    }

    /**
     * 异步发送消息
     */
    @Override
    public void sendAsync(String topic, String message) {
        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException("消息主题不能为空");
        }
        if (message == null) {
            throw new IllegalArgumentException("消息体不能为空");
        }
        log.info("异步发送消息: topic={}", topic);
        rocketMQUtil.asyncSend(topic, message);
    }
}
