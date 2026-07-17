package com.studyback.smartfleet.util;

import com.studyback.smartfleet.exception.BusinessException;
import com.studyback.smartfleet.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * RocketMQ 工具类
 * <p>封装 Producer 发送消息操作，消息体使用 JSON 格式</p>
 */
@Slf4j
@Component
public class RocketMQUtil {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 同步发送消息
     *
     * @param topic   主题
     * @param payload 消息体（对象，将自动序列化为 JSON）
     */
    public void syncSend(String topic, Object payload) {
        log.info("发送同步消息: topic={}, payload={}", topic, payload);
        try {
            rocketMQTemplate.convertAndSend(topic, payload);
        } catch (Exception e) {
            log.error("消息发送失败: topic={}, payload={}", topic, payload, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR,
                    "消息发送失败, topic=" + topic + ", 原因: " + e.getMessage());
        }
    }

    /**
     * 同步发送消息（带 tag）
     *
     * @param topic   主题
     * @param tag     标签
     * @param payload 消息体
     */
    public void syncSend(String topic, String tag, Object payload) {
        String destination = topic + ":" + tag;
        log.info("发送同步消息: destination={}, payload={}", destination, payload);
        try {
            rocketMQTemplate.convertAndSend(destination, payload);
        } catch (Exception e) {
            log.error("消息发送失败: destination={}, payload={}", destination, payload, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR,
                    "消息发送失败, topic=" + topic + ", tag=" + tag + ", 原因: " + e.getMessage());
        }
    }

    /**
     * 异步发送消息
     *
     * @param topic   主题
     * @param payload 消息体
     */
    public void asyncSend(String topic, Object payload) {
        log.info("发送异步消息: topic={}, payload={}", topic, payload);
        rocketMQTemplate.asyncSend(topic, MessageBuilder.withPayload(payload).build(),
                new org.apache.rocketmq.client.producer.SendCallback() {
                    @Override
                    public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
                        log.info("异步消息发送成功: topic={}, msgId={}", topic, sendResult.getMsgId());
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("异步消息发送失败: topic={}", topic, e);
                    }
                });
    }

    /**
     * 发送延迟消息
     *
     * @param topic         主题
     * @param payload       消息体
     * @param delayLevel    延迟级别（1-18，对应不同延迟时间）
     */
    public void syncSendDelay(String topic, Object payload, int delayLevel) {
        log.info("发送延迟消息: topic={}, delayLevel={}, payload={}", topic, delayLevel, payload);
        try {
            Message<Object> message = MessageBuilder.withPayload(payload).build();
            rocketMQTemplate.syncSend(topic, message, 3000, delayLevel);
        } catch (Exception e) {
            log.error("延迟消息发送失败: topic={}, delayLevel={}, payload={}", topic, delayLevel, payload, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR,
                    "延迟消息发送失败, topic=" + topic + ", 原因: " + e.getMessage());
        }
    }
}
