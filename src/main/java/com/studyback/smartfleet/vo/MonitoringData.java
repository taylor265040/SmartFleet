package com.studyback.smartfleet.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 监控数据 VO
 * <p>包含车辆状态统计、订单统计、在线用户数、WebSocket 连接数等实时指标</p>
 */
@Data
public class MonitoringData implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 可用车辆数 */
    private long availableVehicles;

    /** 租赁中车辆数 */
    private long rentingVehicles;

    /** 预留车辆数 */
    private long reservedVehicles;

    /** 充电中车辆数 */
    private long chargingVehicles;

    /** 维修中车辆数 */
    private long repairingVehicles;

    /** 运行中订单数 */
    private long runningOrders;

    /** 今日完成订单数 */
    private long todayCompletedOrders;

    /** 在线用户数 */
    private long onlineUsers;

    /** 活跃 WebSocket 连接数 */
    private int activeWebSocketConnections;

    /** 车辆状态分布（状态 -> 数量） */
    private Map<String, Long> vehicleDistribution;

    /** 订单状态分布（状态 -> 数量） */
    private Map<String, Long> orderStatistics;
}
