package com.studyback.smartfleet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyback.smartfleet.entity.Vehicle;
import com.studyback.smartfleet.response.ApiResponse;
import com.studyback.smartfleet.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 车辆 Controller
 */
@Tag(name = "车辆管理", description = "车辆相关接口")
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * 根据ID查询车辆
     */
    @Operation(summary = "根据ID查询车辆")
    @GetMapping("/{id}")
    public ApiResponse<Vehicle> getById(
            @Parameter(description = "车辆ID") @PathVariable Long id) {
        Vehicle vehicle = vehicleService.getById(id);
        return ApiResponse.success(vehicle);
    }

    /**
     * 根据状态查询车辆列表
     */
    @Operation(summary = "根据状态查询车辆列表")
    @GetMapping
    public ApiResponse<List<Vehicle>> listByStatus(
            @Parameter(description = "车辆状态") @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Vehicle::getStatus, status);
        }
        List<Vehicle> vehicles = vehicleService.list(wrapper);
        return ApiResponse.success(vehicles);
    }
}
