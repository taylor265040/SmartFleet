package com.studyback.smartfleet.service;

/**
 * RocketMQ 消息消费者服务接口
 * <p>处理车辆位置更新、里程上报、订单落库等异步消息</p>
 */
public interface RocketMQConsumerService {

    /**
     * 处理车辆位置更新消息
     *
     * @param message 消息体（JSON 字符串，包含 vehicleId, lat, lng）
     */
    void handleVehicleLocationUpdate(String message);

    /**
     * 处理里程上报消息
     *
     * @param message 消息体（JSON 字符串，包含 vehicleId, mileage）
     */
    void handleMileageReport(String message);

    /**
     * 处理订单落库消息
     *
     * @param message 消息体（JSON 字符串，包含订单信息）
     */
    void handleOrderCreation(String message);
}
