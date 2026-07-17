package com.studyback.smartfleet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyback.smartfleet.entity.Order;
import com.studyback.smartfleet.entity.OrderStatus;
import com.studyback.smartfleet.entity.Vehicle;
import com.studyback.smartfleet.entity.VehicleStatus;
import com.studyback.smartfleet.mapper.OrderMapper;
import com.studyback.smartfleet.mapper.UserMapper;
import com.studyback.smartfleet.mapper.VehicleMapper;
import com.studyback.smartfleet.service.MonitoringService;
import com.studyback.smartfleet.service.WebSocketService;
import com.studyback.smartfleet.vo.MonitoringData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 监控服务实现
 * <p>聚合车辆、订单、用户、WebSocket 连接等数据，提供实时监控指标</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {

    private final VehicleMapper vehicleMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final WebSocketService webSocketService;

    @Override
    public MonitoringData getMonitoringData() {
        MonitoringData data = new MonitoringData();

        // 车辆状态统计
        Map<String, Long> vehicleDist = getVehicleDistribution();
        data.setVehicleDistribution(vehicleDist);
        data.setAvailableVehicles(vehicleDist.getOrDefault(VehicleStatus.AVAILABLE.getValue(), 0L));
        data.setRentingVehicles(vehicleDist.getOrDefault(VehicleStatus.RENTING.getValue(), 0L));
        data.setReservedVehicles(vehicleDist.getOrDefault(VehicleStatus.RESERVED.getValue(), 0L));
        data.setChargingVehicles(vehicleDist.getOrDefault(VehicleStatus.CHARGING.getValue(), 0L));
        data.setRepairingVehicles(vehicleDist.getOrDefault(VehicleStatus.REPAIRING.getValue(), 0L));

        // 订单统计
        data.setRunningOrders(countOrdersByStatus(OrderStatus.RUNNING.getValue()));
        data.setTodayCompletedOrders(countTodayCompletedOrders());
        data.setOrderStatistics(getOrderStatistics());

        // 在线用户数（通过 Redis 统计或简单返回用户总数）
        data.setOnlineUsers(countOnlineUsers());

        // 活跃 WebSocket 连接数
        data.setActiveWebSocketConnections(webSocketService.getActiveSessionCount());

        log.info("获取监控数据: 可用车辆={}, 租赁中车辆={}, 运行中订单={}, 今日完成订单={}, WebSocket连接={}",
                data.getAvailableVehicles(), data.getRentingVehicles(),
                data.getRunningOrders(), data.getTodayCompletedOrders(),
                data.getActiveWebSocketConnections());

        return data;
    }

    @Override
    public Map<String, Long> getVehicleDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (VehicleStatus status : VehicleStatus.values()) {
            LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Vehicle::getStatus, status.getValue());
            long count = vehicleMapper.selectCount(wrapper);
            distribution.put(status.getValue(), count);
        }
        return distribution;
    }

    @Override
    public Map<String, Long> getOrderStatistics() {
        Map<String, Long> statistics = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = countOrdersByStatus(status.getValue());
            statistics.put(status.getValue(), count);
        }
        return statistics;
    }

    /**
     * 按状态统计订单数量
     */
    private long countOrdersByStatus(String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, status);
        return orderMapper.selectCount(wrapper);
    }

    /**
     * 统计今日完成的订单数量
     */
    private long countTodayCompletedOrders() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, OrderStatus.COMPLETED.getValue())
                .ge(Order::getUpdateTime, todayStart);
        return orderMapper.selectCount(wrapper);
    }

    /**
     * 统计在线用户数
     * <p>简单实现：返回数据库中的用户总数，实际应通过 Redis 或 Session 管理</p>
     */
    private long countOnlineUsers() {
        return userMapper.selectCount(null);
    }
}
