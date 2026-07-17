package com.studyback.smartfleet.controller;

import com.studyback.smartfleet.entity.GridDispatch;
import com.studyback.smartfleet.response.ApiResponse;
import com.studyback.smartfleet.service.GridDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网格运力 Controller
 */
@Tag(name = "网格运力管理", description = "网格运力相关接口")
@RestController
@RequestMapping("/api/grid-dispatches")
@RequiredArgsConstructor
public class GridDispatchController {

    private final GridDispatchService gridDispatchService;

    /**
     * 查询所有网格运力
     */
    @Operation(summary = "查询所有网格运力")
    @GetMapping
    public ApiResponse<List<GridDispatch>> list() {
        List<GridDispatch> list = gridDispatchService.list();
        return ApiResponse.success(list);
    }

    /**
     * 根据GeoHash查询网格运力
     */
    @Operation(summary = "根据GeoHash查询网格运力")
    @GetMapping("/{geohash}")
    public ApiResponse<GridDispatch> getByGeohash(
            @Parameter(description = "GeoHash编码") @PathVariable String geohash) {
        GridDispatch dispatch = gridDispatchService.lambdaQuery()
                .eq(GridDispatch::getGeohash, geohash)
                .one();
        return ApiResponse.success(dispatch);
    }

    /**
     * 查询网格供需比
     */
    @Operation(summary = "查询网格供需比")
    @GetMapping("/supply-demand/{geohash}")
    public ApiResponse<Map<String, Object>> getSupplyDemandRatio(
            @Parameter(description = "GeoHash编码") @PathVariable String geohash) {
        double ratio = gridDispatchService.calculateSupplyDemandRatio(geohash);
        boolean isLowSupply = gridDispatchService.isLowSupplyArea(geohash);

        Map<String, Object> result = Map.of(
                "geohash", geohash,
                "supplyDemandRatio", ratio,
                "isLowSupply", isLowSupply
        );
        return ApiResponse.success(result);
    }

    /**
     * 查询车辆分布统计
     */
    @Operation(summary = "查询车辆分布统计")
    @GetMapping("/distribution")
    public ApiResponse<Map<String, Integer>> getVehicleDistribution() {
        Map<String, Integer> distribution = gridDispatchService.getVehicleDistribution();
        return ApiResponse.success(distribution);
    }
}
