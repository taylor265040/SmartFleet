package com.studyback.smartfleet.controller;

import com.studyback.smartfleet.entity.VehicleRecommendation;
import com.studyback.smartfleet.response.ApiResponse;
import com.studyback.smartfleet.service.VehicleScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 车辆推荐 Controller
 * <p>提供地图区域车辆推荐和车辆列表推荐接口</p>
 */
@Tag(name = "车辆推荐", description = "智能车辆推荐接口")
@RestController
@RequestMapping("/api/vehicles/recommend")
@RequiredArgsConstructor
public class VehicleRecommendController {

    private final VehicleScoringService vehicleScoringService;

    /**
     * 地图区域车辆推荐
     * <p>返回指定范围内评分最高的车辆列表，包含坐标信息，用于地图展示</p>
     */
    @Operation(summary = "地图区域车辆推荐", description = "返回指定范围内评分最高的车辆列表，包含坐标信息")
    @GetMapping("/map")
    public ApiResponse<List<VehicleRecommendation>> recommendForMap(
            @Parameter(description = "中心点纬度（-90~90）", required = true)
            @RequestParam double lat,
            @Parameter(description = "中心点经度（-180~180）", required = true)
            @RequestParam double lng,
            @Parameter(description = "搜索半径（公里），默认10")
            @RequestParam(defaultValue = "10") double radius,
            @Parameter(description = "返回数量限制")
            @RequestParam(defaultValue = "20") int limit) {
        List<VehicleRecommendation> recommendations = vehicleScoringService.recommendForMap(lat, lng, radius, limit);
        return ApiResponse.success(recommendations);
    }

    /**
     * 车辆列表推荐
     * <p>返回评分最高的车辆列表，不含坐标信息，用于列表展示</p>
     */
    @Operation(summary = "车辆列表推荐", description = "返回评分最高的车辆列表，不含坐标信息")
    @GetMapping("/list")
    public ApiResponse<List<VehicleRecommendation>> recommendForList(
            @Parameter(description = "用户纬度（-90~90）", required = true)
            @RequestParam double lat,
            @Parameter(description = "用户经度（-180~180）", required = true)
            @RequestParam double lng,
            @Parameter(description = "返回数量限制")
            @RequestParam(defaultValue = "10") int limit) {
        List<VehicleRecommendation> recommendations = vehicleScoringService.recommendForList(lat, lng, limit);
        return ApiResponse.success(recommendations);
    }
}
