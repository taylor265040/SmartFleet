package com.studyback.smartfleet.controller;

import com.studyback.smartfleet.entity.StateChangeRecord;
import com.studyback.smartfleet.entity.StateEvent;
import com.studyback.smartfleet.entity.VehicleStatus;
import com.studyback.smartfleet.response.ApiResponse;
import com.studyback.smartfleet.service.VehicleStateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 车辆状态 Controller
 * <p>提供车辆状态查询、变更、历史记录、统计等接口</p>
 */
@Tag(name = "车辆状态管理", description = "车辆状态机相关接口")
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleStateController {

    private final VehicleStateService vehicleStateService;

    /**
     * 查询车辆当前状态
     * <p>优先从 Redis 缓存获取，缓存未命中则从数据库查询</p>
     */
    @Operation(summary = "查询车辆状态")
    @GetMapping("/{id}/status")
    public ApiResponse<VehicleStatus> getVehicleStatus(
            @Parameter(description = "车辆ID") @PathVariable Long id) {
        VehicleStatus status = vehicleStateService.getVehicleStatus(id);
        return ApiResponse.success(status);
    }

    /**
     * 变更车辆状态
     * <p>通过状态机校验后执行状态转移，乐观锁保证并发安全</p>
     */
    @Operation(summary = "变更车辆状态")
    @PostMapping("/{id}/status")
    public ApiResponse<VehicleStatus> changeVehicleStatus(
            @Parameter(description = "车辆ID") @PathVariable Long id,
            @Parameter(description = "状态转移事件") @RequestParam StateEvent event) {
        VehicleStatus status = vehicleStateService.changeVehicleStatus(id, event);
        return ApiResponse.success(status);
    }

    /**
     * 查询车辆状态变更历史
     */
    @Operation(summary = "查询状态变更历史")
    @GetMapping("/{id}/status/history")
    public ApiResponse<List<StateChangeRecord>> getStateChangeHistory(
            @Parameter(description = "车辆ID") @PathVariable Long id) {
        List<StateChangeRecord> history = vehicleStateService.getStateChangeHistory(id);
        return ApiResponse.success(history);
    }

    /**
     * 按状态统计车辆数量
     */
    @Operation(summary = "按状态统计车辆数量")
    @GetMapping("/status/statistics")
    public ApiResponse<Map<String, Long>> countByStatus() {
        Map<String, Long> statistics = vehicleStateService.countByStatus();
        return ApiResponse.success(statistics);
    }
}
