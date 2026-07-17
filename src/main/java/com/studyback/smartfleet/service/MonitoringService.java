package com.studyback.smartfleet.service;

import com.studyback.smartfleet.vo.MonitoringData;

import java.util.Map;

/**
 * 监控服务接口
 * <p>提供实时监控数据查询功能</p>
 */
public interface MonitoringService {

    /**
     * 获取监控数据
     * <p>包含可用车辆数、租赁中车辆数、预留车辆数、运行中订单数、今日完成订单数、在线用户数、活跃 WebSocket 连接数</p>
     *
     * @return 监控数据
     */
    MonitoringData getMonitoringData();

    /**
     * 获取车辆分布
     * <p>按状态分组统计车辆数量</p>
     *
     * @return 车辆分布（状态 -> 数量）
     */
    Map<String, Long> getVehicleDistribution();

    /**
     * 获取订单统计
     * <p>按状态分组统计订单数量</p>
     *
     * @return 订单统计（状态 -> 数量）
     */
    Map<String, Long> getOrderStatistics();
}
