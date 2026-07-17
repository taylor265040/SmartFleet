package com.studyback.smartfleet.service.impl;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.studyback.smartfleet.entity.Vehicle;
import com.studyback.smartfleet.service.CacheAsideService;
import com.studyback.smartfleet.service.RocketMQConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * RocketMQ 消息消费者服务实现
 * <p>使用 @RocketMQMessageListener 注解消费消息，消费者必须做幂等处理</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RocketMQConsumerServiceImpl implements RocketMQConsumerService {

    private final CacheAsideService cacheAsideService;
    private final ObjectMapper objectMapper;

    /**
     * 处理车辆位置更新消息
     * <p>解析 vehicleId、lat、lng，更新车辆缓存中的位置信息</p>
     */
    @Override
    public void handleVehicleLocationUpdate(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Long vehicleId = node.get("vehicleId").asLong();
            BigDecimal lat = new BigDecimal(node.get("lat").asText());
            BigDecimal lng = new BigDecimal(node.get("lng").asText());

            log.info("处理车辆位置更新: vehicleId={}, lat={}, lng={}", vehicleId, lat, lng);

            // 查询车辆并更新位置
            Vehicle vehicle = cacheAsideService.getVehicle(vehicleId);
            if (vehicle != null) {
                vehicle.setCurrentLat(lat);
                vehicle.setCurrentLng(lng);
                cacheAsideService.updateVehicle(vehicle);
                log.info("车辆位置更新成功: vehicleId={}", vehicleId);
            } else {
                log.warn("车辆不存在, 无法更新位置: vehicleId={}", vehicleId);
            }
        } catch (Exception e) {
            log.error("处理车辆位置更新消息失败: message={}", message, e);
        }
    }

    /**
     * 处理里程上报消息
     * <p>解析 vehicleId、mileage，更新车辆健康度评分</p>
     */
    @Override
    public void handleMileageReport(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Long vehicleId = node.get("vehicleId").asLong();
            int mileage = node.get("mileage").asInt();

            log.info("处理里程上报: vehicleId={}, mileage={}", vehicleId, mileage);

            // 查询车辆并更新健康度（示例逻辑：里程越高健康度越低）
            Vehicle vehicle = cacheAsideService.getVehicle(vehicleId);
            if (vehicle != null) {
                int healthScore = Math.max(0, 100 - (mileage / 1000));
                vehicle.setHealthScore(healthScore);
                cacheAsideService.updateVehicle(vehicle);
                log.info("里程上报处理成功: vehicleId={}, healthScore={}", vehicleId, healthScore);
            } else {
                log.warn("车辆不存在, 无法处理里程上报: vehicleId={}", vehicleId);
            }
        } catch (Exception e) {
            log.error("处理里程上报消息失败: message={}", message, e);
        }
    }

    /**
     * 处理订单落库消息
     * <p>当前仅记录日志，订单实际落库由 OrderService 完成</p>
     */
    @Override
    public void handleOrderCreation(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            log.info("处理订单落库消息: orderId={}", node.has("orderId") ? node.get("orderId").asText() : "unknown");
        } catch (Exception e) {
            log.error("处理订单落库消息失败: message={}", message, e);
        }
    }

    /**
     * 车辆位置更新消费者
     * <p>监听 vehicle-location-update 主题</p>
     */
    @Service
    @RocketMQMessageListener(topic = "vehicle-location-update", consumerGroup = "sf-consumer-vehicle-location")
    @RequiredArgsConstructor
    public static class VehicleLocationConsumer implements RocketMQListener<String> {

        private final RocketMQConsumerService rocketMQConsumerService;

        @Override
        public void onMessage(String message) {
            rocketMQConsumerService.handleVehicleLocationUpdate(message);
        }
    }

    /**
     * 里程上报消费者
     * <p>监听 mileage-report 主题</p>
     */
    @Service
    @RocketMQMessageListener(topic = "mileage-report", consumerGroup = "sf-consumer-mileage")
    @RequiredArgsConstructor
    public static class MileageReportConsumer implements RocketMQListener<String> {

        private final RocketMQConsumerService rocketMQConsumerService;

        @Override
        public void onMessage(String message) {
            rocketMQConsumerService.handleMileageReport(message);
        }
    }

    /**
     * 订单落库消费者
     * <p>监听 order-creation 主题</p>
     */
    @Service
    @RocketMQMessageListener(topic = "order-creation", consumerGroup = "sf-consumer-order")
    @RequiredArgsConstructor
    public static class OrderCreationConsumer implements RocketMQListener<String> {

        private final RocketMQConsumerService rocketMQConsumerService;

        @Override
        public void onMessage(String message) {
            rocketMQConsumerService.handleOrderCreation(message);
        }
    }
}
