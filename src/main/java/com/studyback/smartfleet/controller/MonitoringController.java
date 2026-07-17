package com.studyback.smartfleet.controller;

import com.studyback.smartfleet.response.ApiResponse;
import com.studyback.smartfleet.service.MonitoringService;
import com.studyback.smartfleet.service.WebSocketService;
import com.studyback.smartfleet.vo.MonitoringData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 监控 Controller
 * <p>提供监控数据、车辆分布、订单统计、实时指标等接口</p>
 */
@Tag(name = "监控管理", description = "实时监控数据接口")
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;
    private final WebSocketService webSocketService;

    /**
     * 获取监控数据
     * <p>包含可用车辆数、租赁中车辆数、预留车辆数、运行中订单数、今日完成订单数、在线用户数、WebSocket 连接数</p>
     */
    @Operation(summary = "获取监控数据")
    @GetMapping("/data")
    public ApiResponse<MonitoringData> getMonitoringData() {
        MonitoringData data = monitoringService.getMonitoringData();
        return ApiResponse.success(data);
    }

    /**
     * 获取车辆分布
     * <p>按状态分组统计车辆数量</p>
     */
    @Operation(summary = "获取车辆分布")
    @GetMapping("/vehicles/distribution")
    public ApiResponse<Map<String, Long>> getVehicleDistribution() {
        Map<String, Long> distribution = monitoringService.getVehicleDistribution();
        return ApiResponse.success(distribution);
    }

    /**
     * 获取订单统计
     * <p>按状态分组统计订单数量</p>
     */
    @Operation(summary = "获取订单统计")
    @GetMapping("/orders/statistics")
    public ApiResponse<Map<String, Long>> getOrderStatistics() {
        Map<String, Long> statistics = monitoringService.getOrderStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 获取实时指标
     * <p>返回当前活跃 WebSocket 连接数等实时指标</p>
     * <p>实际实时数据通过 WebSocket 端点 /ws/monitoring 推送</p>
     */
    @Operation(summary = "获取实时指标")
    @GetMapping("/realtime")
    public ApiResponse<Map<String, Object>> getRealtimeMetrics() {
        MonitoringData data = monitoringService.getMonitoringData();
        Map<String, Object> metrics = Map.of(
                "activeWebSocketConnections", data.getActiveWebSocketConnections(),
                "runningOrders", data.getRunningOrders(),
                "availableVehicles", data.getAvailableVehicles(),
                "rentingVehicles", data.getRentingVehicles()
        );
        return ApiResponse.success(metrics);
    }
}
